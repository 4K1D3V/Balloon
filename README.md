# Balloon

![GitHub release (latest by date)](https://img.shields.io/github/v/release/4K1D3V/Balloon)
![GitHub issues](https://img.shields.io/github/issues/4K1D3V/Balloon)
![GitHub license](https://img.shields.io/github/license/4K1D3V/Balloon)

**Balloon** is a Minecraft plugin that adds immersive hot air balloon travel to your server. Players can select balloons, destinations, and passengers, manage fuel and repairs, and experience dynamic flight with wind effects, turbulence, and crash mechanics. Built with PacketEvents for smooth particle and entity animations.

---

## Features

- **Customizable Balloons**: Choose from different balloon variants with unique sizes, capacities, speeds, and health.
- **Dynamic Flight Paths**: Travel between configurable destinations using cubic Bézier curves for smooth trajectories.
- **Wind & Weather Effects**: Wind strength affects flight, with increased turbulence during storms.
- **Fuel Management**: Requires coal to launch and sustain flight, with reserve fuel mechanics.
- **Health & Repairs**: Balloons take damage from turbulence and require coal and wool to repair.
- **Passenger System**: Invite nearby players to join your journey (up to balloon capacity).
- **Crash Mechanics**: Low fuel or health can cause crashes, with slow-falling ejection for safety.
- **Pre-Flight Checklist**: GUI shows balloon health, fuel status, and crash cooldown.
- **Visuals & Sounds**: Flame, cloud, and smoke particles, plus immersive sound effects.

---

## Installation

### Prerequisites
- **Minecraft Server**: Spigot/Paper 1.21+ (tested up to 1.21.x).
- **Java**: 22 or higher.
- **Dependencies**:
    - [PacketEvents](https://github.com/retrooper/packetevents) (included via Maven).

### Steps
1. **Download**: Grab the latest `Balloon.jar` from the [Releases](https://github.com/4K1D3V/Balloon/releases) page.
2. **Install**: Place the JAR in your server's `plugins` folder.
3. **Restart**: Start or restart your server to generate default configuration files.
4. **Configure**: Edit `config.yml` and `message.yml` in `plugins/Balloon/` to customize settings (see [Configuration](#configuration)).

---

## Usage

### Commands
- `/balloon`: Opens the balloon travel GUI to select a balloon, destination, and passengers.
- `/balloon <destination>`: Directly initiates travel to a specified destination (if configured).

### How to Play
1. **Interact**: Right-click a horse named "Balloon" to open the GUI (spawn one manually or configure a spawn system).
2. **Select Balloon**: Choose a balloon type (e.g., `red_small` or `blue_large`).
3. **Pick Destination**: Select a predefined destination (e.g., `village` or `mountain`).
4. **Add Passengers**: Invite nearby players to join (within 10 blocks).
5. **Check Requirements**: Ensure you have enough coal and the balloon is repaired.
6. **Launch**: Click "Confirm Travel" to start the journey!

### In-Game Mechanics
- **Flight**: Balloons follow a curved path with wind sway and bobbing motion.
- **Turbulence**: High winds may cause shaking and damage; watch for warnings.
- **Fuel**: Coal is consumed mid-flight; reserve fuel helps prevent crashes.
- **Crash**: If fuel runs out or health hits zero, the balloon explodes, and players parachute down.

---

## Configuration

### `config.yml`
```yaml
destinations:
  village:
    x: 100
    y: 80
    z: 100
    control1: { x: 50, y: 95, z: 50 }
    control2: { x: 75, y: 100, z: 75 }
  mountain:
    x: 200
    y: 120
    z: 300
    control1: { x: 150, y: 135, z: 150 }
    control2: { x: 175, y: 140, z: 225 }
balloons:
  red_small:
    color: "RED_WOOL"
    size: 1.0
    capacity: 1
    speed: 3
    basket: "OAK_PLANKS"
    fuel: 1
    max_health: 10
  blue_large:
    color: "BLUE_WOOL"
    size: 2.0
    capacity: 4
    speed: 1
    basket: "DARK_OAK_PLANKS"
    fuel: 2
    max_health: 20
cooldown: 60
wind_strength: 1.0
wind_rain_multiplier: 2.0
fuel_wind_threshold: 1.5
fuel_wind_extra: 1
fuel_reserve_max: 2
repair_cost:
  coal: 1
  wool: 2
crash_cooldown: 300
```
- **destinations**: Define travel points with coordinates and control points for Bézier curves.
- **balloons**: Configure balloon types with materials, stats, and costs.
- **cooldown**: Time (seconds) between uses.
- **wind_strength**: Base wind effect on flight path.
- **repair_cost**: Resources needed to fix balloon damage.

### `message.yml`
```yaml
prefix: "&7[&cBalloon&7] "
launching: "&aLaunching in %seconds% seconds..."
arrived: "&aArrived at %destination%!"
invalid-destination: "&cInvalid destination! Available: %destinations%"
cooldown: "&cOn cooldown! Wait %seconds% seconds."
no-fuel: "&cYou need coal to fuel the balloon!"
select-balloon-first: "&cPlease select a balloon and destination first!"
low-fuel: "&cWarning: Low fuel! Add coal or risk crashing!"
turbulence-warning: "&cTurbulence ahead! Brace yourself!"
```
- Customize in-game messages with color codes and placeholders.

---

## Building from Source

### Requirements
- **Maven**: For dependency management and building.
- **Java**: 22.

### Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/4K1D3V/Balloon.git
   cd Balloon
   ```
2. **Build**:
   ```bash
   mvn clean package
   ```
3. **Output**: Find the shaded JAR in `target/Balloon-1.0.0.jar`.

### Dependencies
The `pom.xml` includes:
- **Spigot API**: For Minecraft server integration.
- **PacketEvents**: For particle and entity packet manipulation.

---

## Contributing

Contributions are welcome! Here’s how to get started:
1. **Fork** the repository.
2. **Create** a feature branch (`git checkout -b feature/YourFeature`).
3. **Commit** your changes (`git commit -m "Add YourFeature"`).
4. **Push** to your fork (`git push origin feature/YourFeature`).
5. **Open** a Pull Request.

Please follow the [Code of Conduct](CODE_OF_CONDUCT.md) and report issues via the [Issues](https://github.com/4K1D3V/Balloon/issues) tab.

---

## License

This project is licensed under the [MIT License](LICENSE). See the [LICENSE](LICENSE) file for details.

---

## Credits

- **Author**: [KiteGG](https://github.com/4K1D3V).
- **PacketEvents**: Thanks to [retrooper](https://github.com/retrooper) for the packet library.
