# SwingPokedex

A simple Java Swing Pokédex application that uses [PokéAPI](https://pokeapi.co) to search and display basic Pokémon data (name, ID, types, height, weight, and sprite).

## Prerequisites

- Java 11 or higher
- Maven (if running locally; GitHub Codespaces includes Maven by default)

## Running in GitHub Codespaces

1. Create a new repository named **SwingPokedex**.
2. Copy the files from this template into your repository.
3. In GitHub, click **Code** → **Create codespace on main** (or whichever branch).
4. Once the Codespace starts, open a Terminal and run:
   ```bash
   mvn clean compile exec:java