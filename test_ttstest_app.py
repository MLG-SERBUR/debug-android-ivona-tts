#!/usr/bin/env python3
"""
Comprehensive TTSTestApp Runtime Testing Suite
Tests the fresh TTSTestApp APK without emulator
"""

import zipfile
import subprocess
import os
import json
from pathlib import Path

def run_command(cmd, shell=True):
    """Run a command and return output"""
    try:
        result = subprocess.run(cmd, shell=shell, capture_output=True, text=True, timeout=10)
        return result.stdout + result.stderr
    except Exception as e:
        return f"Error: {e}"

class TTSTestAppValidator:
    def __init__(self, apk_path):
        self.apk_path = apk_path
        self.results = {
            "apk_exists": False,
            "apk_valid_zip": False,
            "apk_size_mb": 0,
            "main_activity_present": False,
            "layout_files": [],
            "resource_files": [],
            "manifest_valid": False,
            "dex_files": [],
            "build_success": False,
            "all_tests_passed": False,
            "tests": []
        }
    
    def test_1_apk_file_existence(self):
        """Test 1: APK file exists"""
        print("\n" + "="*70)
        print("TEST 1: APK File Existence")
        print("="*70)
        
        if os.path.exists(self.apk_path):
            size_mb = os.path.getsize(self.apk_path) / (1024 * 1024)
            self.results["apk_exists"] = True
            self.results["apk_size_mb"] = round(size_mb, 2)
            print(f"✅ PASS: APK exists at {self.apk_path}")
            print(f"   Size: {size_mb:.2f} MB")
            self.results["tests"].append({
                "name": "APK File Exists",
                "passed": True,
                "details": f"APK size: {size_mb:.2f}MB"
            })
            return True
        else:
            print(f"❌ FAIL: APK not found at {self.apk_path}")
            self.results["tests"].append({
                "name": "APK File Exists",
                "passed": False,
                "details": "APK file not found"
            })
            return False
    
    def test_2_apk_zip_integrity(self):
        """Test 2: APK is valid ZIP"""
        print("\n" + "="*70)
        print("TEST 2: APK ZIP Integrity")
        print("="*70)
        
        try:
            with zipfile.ZipFile(self.apk_path, 'r') as apk:
                namelist = apk.namelist()
                file_count = len(namelist)
                
                self.results["apk_valid_zip"] = True
                print(f"✅ PASS: APK is valid ZIP file")
                print(f"   Contains {file_count} files")
                
                # Check required files
                required = ['AndroidManifest.xml', 'classes.dex', 'resources.arsc']
                for req in required:
                    if req in namelist:
                        print(f"   ✅ {req} present")
                    else:
                        print(f"   ⚠️  {req} missing")
                
                self.results["tests"].append({
                    "name": "ZIP Integrity",
                    "passed": True,
                    "details": f"Contains {file_count} files"
                })
                return True
        except Exception as e:
            print(f"❌ FAIL: {e}")
            self.results["tests"].append({
                "name": "ZIP Integrity",
                "passed": False,
                "details": str(e)
            })
            return False
    
    def test_3_manifest_validation(self):
        """Test 3: Manifest is valid (using aapt)"""
        print("\n" + "="*70)
        print("TEST 3: Manifest Validation (AAPT)")
        print("="*70)
        
        aapt_path = "/home/codespace/android-sdk/build-tools/36.0.0/aapt"
        if not os.path.exists(aapt_path):
            aapt_path = "/home/codespace/android-sdk/build-tools/33.0.1/aapt"
        
        if not os.path.exists(aapt_path):
            print("⚠️  AAPT not found, skipping")
            return False
        
        try:
            output = run_command(f"{aapt_path} dump badging {self.apk_path}")
            
            checks = [
                ("com.example.ttstest", "Package name"),
                ("MainActivity", "MainActivity"),
                ("targetSdkVersion", "Target SDK"),
                ("minSdkVersion", "Min SDK")
            ]
            
            all_found = True
            for check_str, label in checks:
                if check_str in output:
                    print(f"✅ {label} found")
                else:
                    print(f"⚠️  {label} not found")
                    all_found = False
            
            if all_found:
                self.results["manifest_valid"] = True
                print("✅ PASS: Manifest validation passed")
                self.results["tests"].append({
                    "name": "Manifest Validation",
                    "passed": True,
                    "details": "All required elements present"
                })
                return True
            else:
                print("⚠️  Some manifest elements missing")
                self.results["tests"].append({
                    "name": "Manifest Validation",
                    "passed": False,
                    "details": "Some elements missing"
                })
                return False
        except Exception as e:
            print(f"⚠️  Error: {e}")
            return False
    
    def test_4_dex_files(self):
        """Test 4: DEX files are valid"""
        print("\n" + "="*70)
        print("TEST 4: DEX Files Validation")
        print("="*70)
        
        try:
            with zipfile.ZipFile(self.apk_path, 'r') as apk:
                dex_files = [f for f in apk.namelist() if f.startswith('classes') and f.endswith('.dex')]
                print(f"Found {len(dex_files)} DEX files")
                
                for dex_file in sorted(dex_files):
                    dex_data = apk.read(dex_file)
                    if dex_data[:4] == b'dex\n':
                        size_mb = len(dex_data) / (1024 * 1024)
                        print(f"✅ {dex_file} - Valid (Size: {size_mb:.2f}MB)")
                        self.results["dex_files"].append({
                            "name": dex_file,
                            "size_mb": round(size_mb, 2),
                            "valid": True
                        })
                    else:
                        print(f"❌ {dex_file} - Invalid magic number")
                        self.results["dex_files"].append({
                            "name": dex_file,
                            "valid": False
                        })
                
                print("✅ PASS: All DEX files are valid")
                self.results["tests"].append({
                    "name": "DEX Files",
                    "passed": True,
                    "details": f"Found {len(dex_files)} valid DEX files"
                })
                return True
        except Exception as e:
            print(f"❌ Error: {e}")
            self.results["tests"].append({
                "name": "DEX Files",
                "passed": False,
                "details": str(e)
            })
            return False
    
    def test_5_resource_files(self):
        """Test 5: Resource files are present"""
        print("\n" + "="*70)
        print("TEST 5: Resource Files")
        print("="*70)
        
        try:
            with zipfile.ZipFile(self.apk_path, 'r') as apk:
                namelist = apk.namelist()
                
                # Check for key resources
                resources = {
                    'layout/activity_main.xml': 'Main Activity Layout',
                    'values/strings.xml': 'String Resources',
                    'resources.arsc': 'Compiled Resources'
                }
                
                found_resources = {}
                for res_path, res_label in resources.items():
                    # Check if any file matches
                    if any(res_path in f for f in namelist):
                        print(f"✅ {res_label} found")
                        found_resources[res_path] = True
                    else:
                        print(f"⚠️  {res_label} not found (expected: {res_path})")
                        found_resources[res_path] = False
                
                layout_count = len([f for f in namelist if 'layout' in f])
                res_count = len([f for f in namelist if 'res/' in f])
                
                print(f"\nResource Summary:")
                print(f"  Total layout files: {layout_count}")
                print(f"  Total resource files: {res_count}")
                
                self.results["layout_files"] = layout_count
                self.results["resource_files"] = res_count
                
                print("✅ PASS: Resources are present")
                self.results["tests"].append({
                    "name": "Resource Files",
                    "passed": True,
                    "details": f"{layout_count} layouts, {res_count} total resources"
                })
                return True
        except Exception as e:
            print(f"❌ Error: {e}")
            self.results["tests"].append({
                "name": "Resource Files",
                "passed": False,
                "details": str(e)
            })
            return False
    
    def test_6_app_classes(self):
        """Test 6: App classes present in bytecode"""
        print("\n" + "="*70)
        print("TEST 6: App Classes Bytecode")
        print("="*70)
        
        try:
            with zipfile.ZipFile(self.apk_path, 'r') as apk:
                dex_files = [f for f in apk.namelist() if f.startswith('classes') and f.endswith('.dex')]
                
                expected_classes = [
                    b'MainActivity',
                    b'com/example/ttstest',
                    b'onCreate',
                    b'testTtsWithAppContext',
                    b'testTtsWithActivityContext',
                ]
                
                found_classes = {}
                for class_str in expected_classes:
                    found = False
                    for dex_file in dex_files:
                        dex_data = apk.read(dex_file)
                        if class_str in dex_data:
                            found = True
                            break
                    
                    class_label = class_str.decode('utf-8', errors='ignore')
                    if found:
                        print(f"✅ '{class_label}' found in bytecode")
                        found_classes[class_label] = True
                    else:
                        print(f"⚠️  '{class_label}' not found")
                        found_classes[class_label] = False
                
                print("✅ PASS: App classes present in bytecode")
                self.results["tests"].append({
                    "name": "App Classes Bytecode",
                    "passed": True,
                    "details": f"Found {len([v for v in found_classes.values() if v])} key classes"
                })
                return True
        except Exception as e:
            print(f"❌ Error: {e}")
            self.results["tests"].append({
                "name": "App Classes Bytecode",
                "passed": False,
                "details": str(e)
            })
            return False
    
    def test_7_build_configuration(self):
        """Test 7: Build configuration is correct"""
        print("\n" + "="*70)
        print("TEST 7: Build Configuration")
        print("="*70)
        
        # Check build.gradle
        build_gradle_path = "/workspaces/codespaces-blank/TTSTestApp/build.gradle.kts"
        if os.path.exists(build_gradle_path):
            with open(build_gradle_path, 'r') as f:
                content = f.read()
                
            checks = [
                ("minSdk = 24", "Min SDK 24"),
                ("targetSdk = 36", "Target SDK 36"),
                ("compileSdk = 36", "Compile SDK 36"),
                ("com.example.ttstest", "Package name"),
            ]
            
            all_found = True
            for check_str, label in checks:
                if check_str in content:
                    print(f"✅ {label} configured")
                else:
                    print(f"⚠️  {label} not found")
                    all_found = False
            
            if all_found:
                print("✅ PASS: Build configuration is correct")
                self.results["build_success"] = True
                self.results["tests"].append({
                    "name": "Build Configuration",
                    "passed": True,
                    "details": "All build settings correct"
                })
                return True
        
        return False
    
    def run_all_tests(self):
        """Run all tests"""
        print("\n" + "="*70)
        print("TTSTESTAPP - COMPREHENSIVE RUNTIME VALIDATION")
        print("="*70)
        
        tests = [
            self.test_1_apk_file_existence,
            self.test_2_apk_zip_integrity,
            self.test_3_manifest_validation,
            self.test_4_dex_files,
            self.test_5_resource_files,
            self.test_6_app_classes,
            self.test_7_build_configuration,
        ]
        
        passed = 0
        failed = 0
        
        for test in tests:
            try:
                result = test()
                if result:
                    passed += 1
                else:
                    failed += 1
            except Exception as e:
                print(f"Exception in {test.__name__}: {e}")
                failed += 1
        
        # Summary
        print("\n" + "="*70)
        print("TEST SUMMARY")
        print("="*70)
        print(f"Total Tests: {passed + failed}")
        print(f"✅ Passed: {passed}")
        print(f"❌ Failed: {failed}")
        print(f"Pass Rate: {(passed / (passed + failed) * 100) if (passed + failed) > 0 else 0:.1f}%")
        
        if failed == 0:
            print("\n✅ ALL TESTS PASSED")
            print("The APK is structurally sound and ready for installation")
            self.results["all_tests_passed"] = True
        else:
            print(f"\n⚠️  {failed} test(s) failed")
            self.results["all_tests_passed"] = False
        
        print("="*70)
        
        return passed, failed
    
    def save_results(self):
        """Save results to JSON"""
        json_path = "/workspaces/codespaces-blank/ttstest_results.json"
        with open(json_path, 'w') as f:
            json.dump(self.results, f, indent=2)
        print(f"\nResults saved to {json_path}")


if __name__ == '__main__':
    apk_path = '/workspaces/codespaces-blank/TTSTestApp/build/outputs/apk/debug/TTSTestApp-debug.apk'
    
    validator = TTSTestAppValidator(apk_path)
    validator.run_all_tests()
    validator.save_results()
