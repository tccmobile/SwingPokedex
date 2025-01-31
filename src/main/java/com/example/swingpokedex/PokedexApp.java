package com.example.swingpokedex;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URL;

public class PokedexApp extends JFrame {

    // Swing components
    private JTextField pokemonNameField;
    private JButton searchButton;
    private JLabel pokemonImageLabel;
    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel typeLabel;
    private JLabel heightLabel;
    private JLabel weightLabel;
    private JLabel errorLabel; // For displaying an error message or image

    public PokedexApp() {
        super("Java Pokédex");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Top panel for user input
        JPanel topPanel = new JPanel();
        pokemonNameField = new JTextField(20);
        searchButton = new JButton("Search Pokémon");

        topPanel.add(pokemonNameField);
        topPanel.add(searchButton);

        // Center panel for displaying Pokémon data
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        pokemonImageLabel = new JLabel();
        nameLabel = new JLabel("Name: ");
        idLabel = new JLabel("ID: ");
        typeLabel = new JLabel("Types: ");
        heightLabel = new JLabel("Height: ");
        weightLabel = new JLabel("Weight: ");
        errorLabel = new JLabel(""); // if there's an error, show a message or an icon

        // Align text to the center
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        heightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        weightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to center panel
        centerPanel.add(pokemonImageLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(nameLabel);
        centerPanel.add(idLabel);
        centerPanel.add(typeLabel);
        centerPanel.add(heightLabel);
        centerPanel.add(weightLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(errorLabel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Button action
        searchButton.addActionListener(this::onSearchPokemon);

        setVisible(true);
    }

    private void onSearchPokemon(ActionEvent event) {
        String pokemonName = pokemonNameField.getText().trim().toLowerCase();

        // Clear previous error/image
        errorLabel.setIcon(null);
        errorLabel.setText("");
        pokemonImageLabel.setIcon(null);

        // Simple validation
        if (pokemonName.isEmpty()) {
            showErrorMessage("Please enter a Pokémon name.");
            return;
        }

        // Perform the search in a background thread
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                searchPokemon(pokemonName);
                return null;
            }
        };
        worker.execute();
    }

    private void searchPokemon(String pokemonName) {
        try {
            // Build the request
            String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + pokemonName;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if we got a valid response
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());

                // Parse data
                String name = json.getString("name");
                int id = json.getInt("id");

                // Pokémon types
                JSONArray typesArray = json.getJSONArray("types");
                StringBuilder typesBuilder = new StringBuilder();
                for (int i = 0; i < typesArray.length(); i++) {
                    JSONObject slotObj = typesArray.getJSONObject(i);
                    JSONObject typeObj = slotObj.getJSONObject("type");
                    typesBuilder.append(typeObj.getString("name"));
                    if (i < typesArray.length() - 1) {
                        typesBuilder.append(", ");
                    }
                }

                int height = json.getInt("height"); // decimeters (according to the API)
                int weight = json.getInt("weight"); // hectograms (according to the API)

                // Sprites
                JSONObject spritesObj = json.getJSONObject("sprites");
                String spriteUrl = spritesObj.getString("front_default");

                // Update GUI
                SwingUtilities.invokeLater(() -> updatePokemonData(
                        name,
                        id,
                        typesBuilder.toString(),
                        height,
                        weight,
                        spriteUrl
                ));
            } else {
                // Non-200 response code
                SwingUtilities.invokeLater(() -> showNotFoundError(pokemonName));
            }
        } catch (IOException | InterruptedException | JSONException e) {
            SwingUtilities.invokeLater(() -> showNotFoundError(pokemonName));
        }
    }

    private void updatePokemonData(String name, int id, String types,
                                   int height, int weight, String imageUrl) {

        // Download sprite image if available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try (InputStream in = new URL(imageUrl).openStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image != null) {
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                    pokemonImageLabel.setIcon(icon);
                }
            } catch (IOException e) {
                showErrorMessage("Could not load sprite image.");
            }
        } else {
            pokemonImageLabel.setIcon(null);
        }

        nameLabel.setText("Name: " + name);
        idLabel.setText("ID: " + id);
        typeLabel.setText("Types: " + types);
        heightLabel.setText("Height (dm): " + height);
        weightLabel.setText("Weight (hg): " + weight);
    }

    private void showNotFoundError(String pokemonName) {
        // You can replace the text error with an error icon if desired
        showErrorMessage("Pokémon \"" + pokemonName + "\" not found!");
        // As an example, display a small "error" image:
        try {
            URL errorImageUrl = new URL("https://via.placeholder.com/150/FF0000/FFFFFF?text=Not+Found");
            BufferedImage image = ImageIO.read(errorImageUrl);
            if (image != null) {
                ImageIcon icon = new ImageIcon(image.getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                errorLabel.setIcon(icon);
            }
        } catch (IOException e) {
            // fallback to text only
        }
    }

    private void showErrorMessage(String message) {
        errorLabel.setText(message);
    }

    public static void main(String[] args) {
        // Run GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(PokedexApp::new);
    }
}