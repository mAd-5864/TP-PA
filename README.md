# JavaFX Chess Game
**Programação Avançada - DEIS/ISEC**

## Description

This is a complete chess game application built in Java with a JavaFX user interface. The project implements all standard chess rules, including piece movements, captures, and check/checkmate detection.

The application is architected around a central `ChessGameManager` that manages game state and logic. It uses the **Memento design pattern** to handle game state snapshots, allowing for robust undo/redo functionality. The game state can also be saved to and loaded from files using Java Serialization.

The UI provides a full graphical chessboard, move history logging, and sound effects for game events.


## Components
* **ChessGameManager**: The main controller class that connects the UI to the data model and manages the game's state machine.
* **ChessGame**: The core data model, containing the board state, piece list, and move history.
* **MainJFX / RootPane**: The main JavaFX application class and the root UI container that assembles all visual components.
* **ChessBoardView**: The JavaFX component responsible for rendering the 8x8 chessboard and its pieces.
* **Memento (Caretaker/Memento)**: Classes that implement the Memento pattern to save and restore `ChessGame` states.
* **ImageManager / SoundManager**: Utility classes for loading and managing visual and audio resources.

## Technologies

* **Java**
* **JavaFX** (for the graphical user interface)
* **Memento Design Pattern** (for state management)
* **Java Serialization** (for saving/loading games)
* **JUnit** (for unit testing)

## Directory Structure

* `src/pt/isec/pa/chess/`: Main application package.
* `src/pt/isec/pa/chess/model/`: Core game logic and state (Model).
* `src/pt/isec/pa/chess/model/data/`: Data structures for the game (Board, Pieces, etc.).
* `src/pt/isec/pa/chess/memento/`: Memento pattern implementation for undo/redo.
* `src/pt/isec/pa/chess/ui/`: JavaFX components (View/Controller).
* `src/pt/isec/pa/chess/ui/res/`: Resource files (images, sounds).
* `reports/`: Project documentation and report (PDF).
* `test/`: Unit tests for the application logic.

## How to Compile and Run

### Prerequisites

1.  **Java Development Kit (JDK)** (e.g., JDK 17 or higher).
2.  **JavaFX SDK**.
3.  An IDE like **IntelliJ IDEA** (recommended, as the project includes `.iml` files).

### Execution (using IDE)

1.  Open the project in your IDE.
2.  Ensure your IDE is configured with the JavaFX SDK. You may need to add the JavaFX libraries to the project structure and configure VM options.
3.  Locate and run the `ChessMain` class.
