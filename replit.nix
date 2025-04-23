
{ pkgs }: {
  deps = [
    pkgs.graalvm17-ce
    pkgs.maven
    pkgs.zip
    pkgs.python3
    pkgs.python3Packages.pip
  ];
}
