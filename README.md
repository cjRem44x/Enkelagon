# Enkelagon Chess

A dragon-themed chess GUI with Stockfish integration, built with Java Swing.

## Features

- **Play against Stockfish** - Choose to play as White, Black, or Random
- **AI Difficulty Settings** - Easy, Medium, Hard presets + advanced options (threads, hash, skill level, depth)
- **Real-time Analysis** - Engine evaluation bar and principal variation display
- **Move Hints** - Get suggested moves from Stockfish
- **Move History** - Algebraic notation with clickable moves
- **Save/Load Games** - PGN format with quicksave support
- **Animated Background** - Dynamic particles and waves
- **Semi-transparent Board** - See the animated background through the tiles

## Screenshots

*Coming soon*

## Requirements

- Java 17 or higher
- Maven 3.6+
- Stockfish 17 (included in `res/stockfish17/`)

## Building

```bash
# Clone the repository
git clone https://github.com/yourusername/enkelagon.git
cd enkelagon

# Build with Maven
mvn clean compile
```

## Running

### Windows
```bash
scripts\run.bat
```

### Linux/macOS
```bash
chmod +x scripts/run.sh
scripts/run.sh
```

### Or with Maven directly
```bash
mvn exec:java -Dexec.mainClass="com.enkelagon.App"
```

## Controls

| Action | Shortcut |
|--------|----------|
| New Game | Ctrl+N |
| Load PGN | Ctrl+O |
| Quicksave | Ctrl+S |
| Save As | Ctrl+Shift+S |
| Undo Move | Ctrl+Z |
| Flip Board | Ctrl+F |

## Project Structure

```
enkelagon/
├── src/main/java/com/enkelagon/
│   ├── App.java              # Entry point
│   ├── config/               # Configuration management
│   ├── engine/               # Stockfish integration
│   ├── logic/                # Game logic, FEN/PGN parsing
│   ├── model/                # Chess model (Board, Piece, Move, Game)
│   └── ui/                   # Swing UI components
├── src/main/resources/       # Icons, settings
├── res/
│   ├── img/                  # Piece images
│   └── stockfish17/          # Stockfish executable
└── scripts/                  # Build and run scripts
```

## Configuration

Settings are stored in `src/main/resources/config/settings.json`:
- Theme colors
- Font settings
- Board preferences
- Engine defaults

## License

- **Code:** MIT License - see [LICENSE](LICENSE)
- **Chess Pieces:** [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) - from [Green Chess](https://greenchess.net/info.php?item=downloads), originally derived from Wikipedia

## Acknowledgments

- [Stockfish](https://stockfishchess.org/) - The powerful open-source chess engine
- [FlatLaf](https://www.formdev.com/flatlaf/) - Modern Swing look and feel
- [Green Chess](https://greenchess.net/) - Chess piece images by Uray M. János
