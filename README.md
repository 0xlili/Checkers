# Checkers Game

This is a basic implementation of the classic game of Checkers (also known as Draughts) built using Java Swing. The game features full rule enforcement, multi-capture logic, and an undo/redo history system.

## Project Features

This project implements the core rules and several advanced features:

### Core Gameplay
* **Board Setup and Rendering**: A standard 8x8 checkers board with pieces placed in their starting positions.
* **Basic Movement**: Click-to-select and click-to-move piece logic.
* **Piece Capturing**: Standard diagonal jump capture mechanic.
* **King Promotion**: Pieces reaching the opposite side of the board are crowned King.
* **Multi-Jump Captures**: Mandatory and optional chain-captures in a single turn.
* **Turn Management**: Turns strictly alternate between Red and Black.
* **Game Over Detection**: The game ends when one player has no pieces remaining.

### User Experience & Polish
* **Undo/Redo**: Full move history tracking via buttons and keyboard shortcuts (Ctrl+Z/Y).
* **Restart**: A dedicated button to reset the board and start a new game.
* **Visual Feedback**: A yellow highlight indicates the currently selected piece.
* **Sound Effects**: Audio feedback for movement, capturing, kinging, game over, and invalid actions.

### Version Control Standard
* **Conventional Commits**: The project's commit history follows the **Conventional Commits** specification for standardized, readable messages.

***

## Setup & Dependencies

### Prerequisites
1.  **Java Development Kit (JDK)**: You must have a JDK (version 8 or higher is recommended) installed on your system to compile and run the Java files.
2.  **Sound Files**: The game requires a set of WAV audio files for sound effects. These files must be placed in a **`/sounds`** directory on the classpath.
    The required sound files are: `move.wav`, `capture.wav`, `king.wav`, `gameover.wav`, and `no.wav` (for invalid moves).

### Directory Structure

Checkers/
├── Checkers.java
├── CheckersBoard.java
├── CheckersPanel.java
└── SoundManager.java

sounds/
├── move.wav
├── capture.wav
├── king.wav
├── gameover.wav
└── no.wav




***

## Step-by-Step Instructions to Run the Game

1.  **Navigate to the Root Directory**
    Open your terminal or command prompt and change the directory to the main **`Checkers`** folder (the one containing the package folder and the `sounds` folder).

2.  **Compile the Java Files**
    Compile all source files from the root directory. This command will find the files inside the `Checkers` package folder.

    ```bash
    javac Checkers/*.java
    ```

3.  **Run the Application**
    Execute the main class using its fully qualified name (`PackageName.ClassName`).

    ```bash
    java Checkers.Checkers
    ```

    The Checkers game window should open, and the game will begin with the Red player's turn.

***

## Key Features to Test

| Feature | How to Access / Test | Access Method |
| :--- | :--- | :--- |
| **Undo/Redo Last Move** | Click the **Undo** or **Redo** buttons in the control panel. | **Buttons** or **Keyboard (Ctrl+Z / Ctrl+Y)** |
| **Multi-Jump Captures** | Position a piece to have multiple consecutive captures in one move. | **Mouse Click** (The piece remains selected until all possible jumps are taken.) |
| **Restart Game** | Click the **Restart** button in the control panel at any point during the game. | **Button Click** |
| **Visual Feedback** | Click a piece to select it. | **Mouse Click** (Selected piece is highlighted in yellow.) |
| **Sound Effects** | Execute a valid move, capture, king promotion, or invalid action. | **Automatic Audio Playback** |