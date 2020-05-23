<p align="center">
  <img src="https://i.imgur.com/l9oLRbS.png">
</p>
<p align="center">
  <b>Dependencies:</b> <a href="https://www.spigotmc.org/resources/protocollib.1997/">ProtocolLib</a>
</p>

Prevention is the best measure, so prevent hacked clients from seeing information that vanilla clients normally cannot see! Currently it hides:
- Health and absorption
- Item names and lore
- Enchantments
- Stack sizes
- Durability
- Potion data
- Status effects

<p align="center">
  <img src="https://i.imgur.com/bSfiVND.png">
  <img src="https://i.imgur.com/YQSraOk.png">
</p>

## Usage
It will automatically strip unnecessary information sent to clients.

## Config
```yaml
# What metadata should be hidden from hacked clients?
hideStackSize: true
hideDisplayName: true
hideLore: true
hideUnbreakable: true
hideEnchantments: true
hideDurability: true
hidePotionData: true
hideHealth: true
hideStatusEffects: true
```

## Limitations
Certain information is not hidden as I do not have access to a hacked client that can see this information to be able to test it, the list includes:
- Pet owners
- Horse attributes

## Commands
**/metahider info** - Plugin info

**/metahider reload** - Reload config (OP)

## Permissions
metahider.reload (OP)
