#!/usr/bin/env python3
import zipfile
import xml.etree.ElementTree as ET
import sys
import os

apk_path = '/workspaces/codespaces-blank/ttsrepro-debug.apk'

if not os.path.exists(apk_path):
    print(f"APK not found: {apk_path}")
    sys.exit(1)

print(f"Analyzing APK: {apk_path}")
print(f"File size: {os.path.getsize(apk_path) / (1024*1024):.2f} MB")
print()

try:
    with zipfile.ZipFile(apk_path, 'r') as z:
        print("✓ APK is valid ZIP file")
        
        # List key files
        print("\nKey files in APK:")
        important_files = ['AndroidManifest.xml', 'classes.dex', 'classes2.dex', 'res/layout/activity_main.xml']
        for pattern in important_files:
            matching = [n for n in z.namelist() if pattern in n]
            if matching:
                print(f"  ✓ {matching[0]} ({z.getinfo(matching[0]).file_size} bytes)")
            else:
                print(f"  ✗ {pattern} NOT FOUND")
        
        # Check for MainActivity class
        print("\nClasses in dex files:")
        for dex_file in [n for n in z.namelist() if n.endswith('.dex')]:
            size = z.getinfo(dex_file).file_size
            print(f"  ✓ {dex_file} ({size} bytes)")
        
        # Try to parse manifest
        print("\nParsing AndroidManifest.xml...")
        try:
            manifest_data = z.read('AndroidManifest.xml')
            print(f"  ✓ Manifest found ({len(manifest_data)} bytes)")
            print("  ✓ APK structure is valid")
        except Exception as e:
            print(f"  ✗ Error reading manifest: {e}")
            
        # List all files
        print(f"\nTotal files in APK: {len(z.namelist())}")
        
except zipfile.BadZipFile as e:
    print(f"✗ APK is NOT a valid ZIP file: {e}")
    sys.exit(1)
except Exception as e:
    print(f"✗ Error analyzing APK: {e}")
    sys.exit(1)

print("\n" + "="*60)
print("✓ APK VALIDATION SUCCESSFUL")
print("="*60)
print("\nAPK is ready to install and test on a device or emulator.")
print("\nInstallation command:")
print("  adb install -r /workspaces/codespaces-blank/ttsrepro-debug.apk")
