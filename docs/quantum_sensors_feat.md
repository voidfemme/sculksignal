# Quantum Sculk Sensors Feature Specification

## Overview
Quantum Sculk Sensors allow players to create "entangled" sensor pairs that can transmit signals across distances beyond the normal 16-block calibrated sculk sensor range, while maintaining 100% vanilla compatibility.

## Core Concept
Players can craft quantum-linked sculk sensor pairs using a nether star. These sensors bypass normal range limitations but are restricted to a 3x3 chunk area to prevent abuse.

## Crafting Recipe

```
[ ] [ ] [ ]
[📡] [⭐] [📡]
[ ] [ ] [ ]
```

Where:
- 📡 = Calibrated Sculk Sensor
- ⭐ = Nether Star
- [ ] = Empty slot

**Input:** 2 Calibrated Sculk Sensors + 1 Nether Star  
**Output:** 2 Quantum Sculk Sensors (with identical quantum IDs)

## Implementation Details

### Crafting Process
1. **Detection:** Plugin detects the crafting pattern via `CraftItemEvent`
2. **Cancellation:** Cancel default crafting behavior
3. **ID Generation:** Generate unique quantum ID (e.g., "QS-12abc345")
4. **Item Creation:** Create two identical quantum sensors with custom names
5. **Registration:** Register the quantum pair with the crafter's UUID
6. **Material Consumption:** Remove crafting materials from table

### Quantum Sensor Properties
- **Item Type:** Normal `CALIBRATED_SCULK_SENSOR` (vanilla compatible)
- **Display Name:** `§dQuantum Sculk Sensor §7[QS-12abc345]`
- **Lore:** `§7Quantum entangled with its pair`
- **Unique ID:** Embedded in the display name for identification

### Placement Restrictions
- **Owner Verification:** Only the original crafter can place quantum sensors
- **Range Limit:** Both sensors must be within a 3x3 chunk area of each other
- **Pairing Validation:** Plugin validates quantum ID authenticity (prevents anvil cheating)

### Quantum Linking Behavior
- **Signal Transmission:** When one quantum sensor activates, its pair also activates
- **Frequency Preservation:** Signal strength/frequency passes through quantum link
- **Chunk Loading:** Plugin loads chunks for both sensors during transmission
- **Range Override:** Quantum link bypasses the 16-block sculk sensor limit

## Anti-Cheat Measures

### Crafting Verification
- **UUID Tracking:** Each quantum ID is tied to the crafter's UUID
- **Placement Validation:** Only original crafter can place quantum sensors
- **Name Tampering Prevention:** Plugin verifies quantum ID authenticity vs registry

### Range Limitations
- **3x3 Chunk Restriction:** Sensors must stay within 3x3 chunk area
- **Link Breaking:** If sensors move outside range, quantum link is severed
- **Re-linking Requirements:** Broken links require re-crafting with new nether star

## Vanilla Compatibility

### With Plugin Present
- Full quantum linking functionality
- Custom crafting recipe works
- Signal transmission across quantum pairs
- Chunk loading for transmission paths

### Without Plugin (World Downloads)
- Sensors exist as normal calibrated sculk sensors
- Function within standard 16-block range
- No quantum linking (graceful degradation)
- All placed infrastructure remains intact

### Future Modloader Support
Since quantum sensors are vanilla items with custom names, any modloader could:
- Detect the same naming pattern
- Implement identical quantum linking logic
- Maintain cross-platform compatibility

## Technical Components

### New Classes Required
```
com.sculksignal.quantum/
├── QuantumRegistry.java          # Manages quantum pairs and ownership
├── QuantumCraftingListener.java  # Handles custom crafting detection
├── QuantumPair.java             # Represents linked sensor pair
└── QuantumSignalHandler.java    # Handles quantum signal transmission
```

### Configuration Options
```yaml
quantum:
  enabled: true
  max-chunk-distance: 1  # 3x3 chunk area (1 chunk in each direction)
  require-nether-star: true
  quantum-id-prefix: "QS-"
  link-broken-message: "§cQuantum link severed - sensors too far apart!"
```

### Database Schema
```yaml
quantum_pairs:
  quantum_id: string (primary key)
  crafter_uuid: string
  sensor1_location: location (nullable until placed)
  sensor2_location: location (nullable until placed)
  created_timestamp: long
  active: boolean
```

## Use Cases

### Telegraph Infrastructure
- **River Crossings:** Bridge signal gaps across water without underwater cables
- **Mountain Passes:** Connect signal paths across terrain obstacles
- **Base Connections:** Link distant areas within the same region

### Balanced Limitations
- **Expensive Cost:** Nether star requirement limits overuse
- **Range Restriction:** 3x3 chunk limit prevents continent-spanning abuse
- **One-Time Use:** Broken links require new nether star to re-establish

## User Experience

### Crafting Flow
1. Obtain nether star (expensive end-game material)
2. Place crafting pattern in crafting table
3. Receive two quantum sensors with matching IDs
4. Place sensors within 3x3 chunk area
5. Enjoy quantum signal transmission

### Visual Feedback
- **Crafting:** Clear display names show quantum IDs
- **Placement:** Chat message confirms quantum link establishment
- **Status:** `/sculksignal info` shows quantum link status
- **Breaking:** Warning when sensors move out of range

## Future Enhancements

### Advanced Features (Future Versions)
- **Quantum Networks:** Allow more than 2 sensors per quantum group
- **Frequency Filtering:** Quantum links that only pass specific frequencies
- **Visual Indicators:** Particle effects between linked quantum sensors
- **Quantum Diagnostics:** Commands to test and debug quantum links

### Integration Possibilities
- **World Map Integration:** Show quantum links on dynmap
- **Metrics Tracking:** Monitor quantum link usage and performance
- **Admin Tools:** Commands to manage and debug quantum infrastructure

## Implementation Priority
**Phase 1 (MVP):** Basic quantum pair crafting and linking  
**Phase 2:** Chunk loading integration and signal transmission  
**Phase 3:** Anti-cheat measures and edge case handling  
**Phase 4:** Polish, configuration, and user experience improvements
