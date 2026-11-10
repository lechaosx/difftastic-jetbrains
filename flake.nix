{
  description = "JetBrains plugin: semantic diffs via difftastic";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = {
    self,
    nixpkgs,
  }: let
    system = "x86_64-linux";
    pkgs = nixpkgs.legacyPackages.${system};
  in {
    devShells.${system}.default = pkgs.mkShell {
      packages = with pkgs; [jdk21 gradle_9];
      LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath (with pkgs; [
        libx11
        libxext
        libxrender
        libxtst
        libxi
        libxrandr
        libxfixes
        libxcursor
        libGL
        fontconfig
        freetype
      ]);
    };
  };
}
