# 🤖 Robot World Simulator 🚀

Welcome to the **Robot World Simulator**, where robots battle, explore, and survive in a dangerous world filled with obstacles!

## 🔥 Features

- **Robot Commands**: Move (`forward`, `back`), turn (`left`, `right`), `fire`, `look`, `reload`, and `repair`!
- **Dangerous World**: Navigate around mountains, lakes, and deadly pits!
- **Multiplayer**: Connect multiple clients and compete (or cooperate?).
- **Server Admin**: Use the server console to monitor robots and world state.
- **Persistence**: Save and restore maps using a relational database.
- **Web API**: Interact with Robot World through a RESTful API.
- **CI/CD Integration**: Fully tested with automated build and deployment pipelines.
- **ORM Support**: Hibernate is used for data persistence, ensuring clean separation of data access.

## 🎮 How to Play

### 1. Launch a Robot

```
launch [make] [name]
```

*(e.g., `launch Sniper Robo-1`)*

### 2. Explore & Fight

- `look` – Scan your surroundings.
- `forward` – Move forward by n steps.
- `back` – Move backward by n steps.
- `fire` – Shoot lasers! (Ammo is limited.)
- `reload` – Restock your weapons.
- `repair` – Fix damage to your robot.

### 3. Don't Die

- Watch your **shields** (they block damage).
- Avoid **pits** (instant death!).

## 🛠️ Server Commands (Admin Only)

- `dump` – View the entire world state.
- `robots` – List all active robots.
- `restore` – Load a saved world from the database.
- `save` – Save current world to database.
- `worlds` – Display all saved worlds from database.
- `quit` – Shut down the server.
- `help` – Show this help message.

## 🚀 Quick Start

```bash
# Run the server normally
java -jar RobotWorldServer.jar

# Run a client
java -jar RobotWorldClient.jar

# Run the reference server with custom arguments
make run-server ARGS="-p 5000 -s 12 -o 3,4"
```

## 📝 Testing with Makefile

```bash
# Run only tests that work with second reference server
make test-second-reference

# Run only tests that work with reference server
make test-reference

# Run all tests that need your server (including client tests)
make test-local
```

## 📜 Fun Fact

The `Pit` obstacle instantly destroys any robot that steps on it. **No second chances!**

## ⚙️ Project Notes

- The project was developed in **iterations**, gradually adding CI/CD, database persistence, and web API features.
- ORM was implemented using **Hibernate** due to compatibility issues with EoDSQL.
- Automated acceptance tests were implemented to ensure server compliance with the Robot World protocol.

---

Made with ❤️ (and maybe too much coffee) by your friendly neighborhood robot overlords. 🤖☕
