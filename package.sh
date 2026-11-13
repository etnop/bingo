#!/usr/bin/env bash
# Gera os executáveis do Bingo, ambos com Java incluído:
#   dist/Bingo-x86_64.AppImage   (Linux: um ficheiro, chmod +x e correr)
#   dist/Bingo.exe               (Windows: um ficheiro, duplo clique)
# O Windows é gerado no WSL com o jpackage.exe de um JDK Windows (WIN_JDK)
# e depois empacotado num só .exe com o warp-packer.
set -euo pipefail
cd "$(dirname "$0")"

WIN_JDK=${WIN_JDK:-/mnt/c/Users/$USER/AppData/Local/Temp/bingo-build/jdk-25.0.5+8}
TOOLS=target/tools
OPTS="--enable-native-access=ALL-UNNAMED"

mkdir -p "$TOOLS"
[[ -x $TOOLS/appimagetool ]] || { curl -sSL -o $TOOLS/appimagetool \
  https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage; chmod +x $TOOLS/appimagetool; }
[[ -x $TOOLS/warp-packer ]] || { curl -sSL -o $TOOLS/warp-packer \
  https://github.com/dgiagio/warp/releases/download/v0.3.0/linux-x64.warp-packer; chmod +x $TOOLS/warp-packer; }

./mvnw -q -DskipTests package
rm -rf dist target/jp-in target/jp-linux target/Bingo.AppDir && mkdir -p dist target/jp-in
cp target/Bingo-2.0.0.jar target/jp-in/bingo.jar

echo "== Linux (AppImage)"
jpackage --type app-image --name Bingo --input target/jp-in --main-jar bingo.jar --app-version 2.0.0 \
  --dest target/jp-linux --java-options "$OPTS"
APPDIR=target/Bingo.AppDir
cp -r target/jp-linux/Bingo "$APPDIR"
cp src/main/resources/fxml/ellipse21.png "$APPDIR/bingo.png"
cat > "$APPDIR/AppRun" <<'EOF'
#!/bin/sh
exec "$(dirname "$(readlink -f "$0")")/bin/Bingo" "$@"
EOF
chmod +x "$APPDIR/AppRun"
cat > "$APPDIR/bingo.desktop" <<'EOF'
[Desktop Entry]
Type=Application
Name=Bingo
Exec=Bingo
Icon=bingo
Categories=Game;
EOF
# WSL não tem FUSE: corre o appimagetool extraído
APPIMAGE_EXTRACT_AND_RUN=1 ARCH=x86_64 $TOOLS/appimagetool -n "$APPDIR" dist/Bingo-x86_64.AppImage

if [[ -x "$WIN_JDK/bin/jpackage.exe" ]]; then
  echo "== Windows (exe)"
  W=$(dirname "$WIN_JDK")
  rm -rf "$W/in" "$W/out" && mkdir -p "$W/in"
  cp target/jp-in/bingo.jar "$W/in/"
  "$WIN_JDK/bin/jpackage.exe" --type app-image --name Bingo \
    --input "$(wslpath -w "$W/in")" --main-jar bingo.jar --app-version 2.0.0 \
    --dest "$(wslpath -w "$W/out")" --java-options "$OPTS"
  # junta a pasta num único .exe (extrai-se sozinho na 1ª execução)
  $TOOLS/warp-packer --arch windows-x64 --input_dir "$W/out/Bingo" --exec Bingo.exe --output dist/Bingo.exe
else
  echo "!! JDK Windows não encontrado em $WIN_JDK — Windows ignorado"
fi
ls -lh dist
