Fork from [Red Horizon](https://github.com/ultraq/redhorizon), mainly to make it compile again.

This serves as a file converter from/to ancient file types used in Westwood games, most notably Command & Conquer Red Alert and DuneII.

I mainly use this for DuneII so the following doc is focused on DuneII

## File types in DuneII

- ENG: texts
- VOC: wave files, can be played e.g. with VLC
- CPS: background pictures
- SHP: sprites of units

## How to build

```
git clone https://github.com/philippkeller/redhorizon.git
cd redhorizon/Projects/Red\ Horizon\ -\ Filetypes/
make
cd ../../
gradle createBundleLayout
```

## How to convert

