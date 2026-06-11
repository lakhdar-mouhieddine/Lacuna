# 🌌 Lacuna

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-MVC-green.svg)](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller)

**Lacuna** is a highly polished, interactive desktop and online multiplayer board game implemented in Java. Inspired by the tactical spatial-claiming game mechanics, players compete to place pawns and claim matching flowers on a circular mat, leveraging geometry, proximity, and foresight.

This project features a complete **Model-View-Controller (MVC)** architecture, a custom-rendered **Swing GUI** with fluid UI animations, a highly optimized **Minimax AI** opponent, and a custom **WebSocket-based network module** for online multiplayer matches.

---

## 📖 Game Rules & Mechanics

The goal of the game is to claim the majority of colors on a circular mat.

1. **The Setup**: 
   - The board contains **49 flowers** divided into **7 colors** (7 flowers per color).
   - Each player has a pool of **6 pawns**.
2. **Placing Phase**:
   - Players take turns placing a pawn on the board.
   - A pawn must be placed on a **straight line** connecting two unclaimed flowers of the **same color**.
   - **Constraint**: The line must be unobstructed (no other flower or pawn can lie inside its collision radius).
   - Placing a pawn instantly captures the two flowers defining that line.
3. **Resolving Phase**:
   - Once all 12 pawns are placed, the remaining flowers on the mat are captured by the player whose pawn is **closest** to that flower.
4. **Scoring & Winning**:
   - For each color, the player who captured the majority of flowers (at least 4 out of 7) wins that color's majority.
   - The player who wins the majority of **at least 4 colors** wins the game.

---

## ✨ Key Features

* **🎨 Rich Swing GUI**: Custom-rendered graphics via 2D vector drawing (`BoardRenderer.java`) featuring smooth anti-aliased circles, connection indicators, and clean typography.
* **⚡ Fluid Animation Engine**:
  * *Intro Animators*: Interactive intro sequences for loading board mats and flowers.
  * *Placement & Splash Animators*: Tactile visual feedback when pawns drop, splash screens, and sliding "toast" notifications.
* **🧠 Smart AI Engine**:
  * **Minimax Algorithm**: A decision-making engine with **Alpha-Beta pruning** for deep move lookaheads.
  * **Transposition Tables**: State caching to avoid redundant evaluations and enhance search speed.
  * **Parameter Sweeper**: A built-in parameter tuning tool that runs sweep simulations, records results in `.csv` formats, and plots tuning statistics to mathematically optimize the AI's heuristics.
* **🌐 Multiplayer Network Module**: 
  * Play local matches vs. another human or the AI.
  * Establish WebSocket/HTTP connections to set up public/private online rooms, list open lobbies, and play in real-time.
* **↩️ Undo / Redo**: Built-in move history stack allowing players to revert and replay moves during local gameplay.

---

## 🛠️ Project Architecture

The codebase strictly follows the **MVC (Model-View-Controller)** pattern:

* **Model (`lacuna.model`)**: Holds state logic, phase machine transitions, proximity resolution algorithms, and state history.
* **View (`lacuna.view`)**: Governs the UI, window layouts, interactive menus, responsive rendering, and custom particle/intro animators.
* **Controller (`lacuna.controller`)**: The orchestrator handling event dispatching and synchronizing the model state with view updates, network packets, or AI decisions.
* **AI (`lacuna.model.AI`)**: Generates valid moves, evaluates state weights, and computes optimal responses.
* **Network (`lacuna.network`)**: Manages communication protocols, JSON serialization, and server-peer connections.

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK) 17 or higher**
* **Git** (for version control)

### 📦 Compilation

To compile all files into a `bin/` directory:

```bash
# Create bin directory
mkdir bin

# Compile all modules
javac -d bin src/lacuna/model/*.java src/lacuna/model/AI/*.java src/lacuna/view/*.java src/lacuna/view/menu/*.java src/lacuna/view/board/*.java src/lacuna/controller/*.java src/lacuna/network/*.java
```

### 🎮 Running the Game

To launch the game Main Frame:

```bash
java -cp bin lacuna.view.MainFrame
```

### 📦 Creating an Executable JAR

To pack the game along with its graphic assets into a standalone, executable JAR file:

1. Create a temporary manifest file named `manifest.txt`:
   ```text
   Manifest-Version: 1.0
   Main-Class: lacuna.view.MainFrame
   
   ```
2. Build the JAR package, including the compiled files from `bin/` and the asset resources:
   ```bash
   jar cfm Lacuna.jar manifest.txt -C bin . -C . assets
   ```
3. Remove the temporary manifest file:
   ```bash
   rm manifest.txt
   ```
4. Run the executable JAR:
   ```bash
   java -jar Lacuna.jar
   ```

---

## 👥 Authors & Credits
Developed as part of the Programmation 6 Course (2025-2026) by Groupe-14.