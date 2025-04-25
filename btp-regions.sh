#!/bin/bash

# This script helps adjust the manifest.yml and deployment settings
# for different SAP BTP regions

# Available SAP BTP regions and their API endpoints
declare -A regions=(
  ["eu10"]="api.cf.eu10.hana.ondemand.com"
  ["eu20"]="api.cf.eu20.hana.ondemand.com"
  ["us10"]="api.cf.us10.hana.ondemand.com"
  ["us20"]="api.cf.us20.hana.ondemand.com"
  ["ap10"]="api.cf.ap10.hana.ondemand.com"
  ["ap11"]="api.cf.ap11.hana.ondemand.com"
  ["ap12"]="api.cf.ap12.hana.ondemand.com"
  ["ap20"]="api.cf.ap20.hana.ondemand.com"
  ["ap21"]="api.cf.ap21.hana.ondemand.com"
  ["jp10"]="api.cf.jp10.hana.ondemand.com"
  ["jp20"]="api.cf.jp20.hana.ondemand.com"
  ["br10"]="api.cf.br10.hana.ondemand.com"
  ["ca10"]="api.cf.ca10.hana.ondemand.com"
  ["ch20"]="api.cf.ch20.hana.ondemand.com"
)

# Function to list available regions
list_regions() {
  echo "Available SAP BTP regions:"
  for region in "${!regions[@]}"; do
    echo "  $region: ${regions[$region]}"
  done
}

# Function to update manifest.yml for a specific region
update_manifest() {
  local region=$1
  local api_endpoint=${regions[$region]}
  
  if [ -z "$api_endpoint" ]; then
    echo "Error: Unknown region '$region'"
    list_regions
    return 1
  fi
  
  # Back up manifest
  cp manifest.yml manifest.yml.bak
  
  # Update route in manifest.yml
  sed -i "s/quizmaster-\${random-word}\.cfapps\.[^\.]*\.hana\.ondemand\.com/quizmaster-\${random-word}\.cfapps.$region.hana.ondemand.com/g" manifest.yml
  
  echo "Updated manifest.yml for region $region"
  echo "Route: quizmaster-\${random-word}.cfapps.$region.hana.ondemand.com"
}

# Main script
if [ $# -eq 0 ]; then
  echo "Usage: $0 <region> | list"
  echo "Example: $0 eu10"
  echo "Example: $0 list"
  exit 1
fi

if [ "$1" == "list" ]; then
  list_regions
  exit 0
fi

update_manifest "$1"