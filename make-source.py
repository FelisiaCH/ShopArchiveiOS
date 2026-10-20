#!/usr/bin/env python3
# Writes source.json, the AltStore Classic source of the release: python3 make-source.py <tag> <ShopArchive.ipa> <the built app's Info.plist> > source.json
# The version and the build come from that Info.plist, so they are what the installed app says it is; the permission texts too.
import json
import os
import plistlib
import sys
from datetime import datetime, timezone

tag, ipa, plist_path = sys.argv[1:4]
with open(plist_path, "rb") as f:
    plist = plistlib.load(f)

repo = "FelisiaCH/ShopArchiveiOS"
print(json.dumps({
    "name": "ShopArchive",
    "identifier": "xyz.felismp.shoparchive.source",
    "apps": [{
        "name": "ShopArchive",
        "bundleIdentifier": "xyz.felismp.shoparchive",
        "developerName": "Felisia",
        "localizedDescription": "The ShopArchive app for the shop's own server: record the day's sales and expenses, read slips with the camera, close the day.",
        "iconURL": f"https://raw.githubusercontent.com/{repo}/main/ShopArchive/Assets.xcassets/AppIcon.appiconset/AppIcon.png",
        "versions": [{
            "version": plist["CFBundleShortVersionString"],
            "buildVersion": plist["CFBundleVersion"],
            "date": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
            "downloadURL": f"https://github.com/{repo}/releases/download/{tag}/ShopArchive.ipa",
            "size": os.path.getsize(ipa),
            "minOSVersion": "16.0",
        }],
        "appPermissions": {
            "entitlements": [],
            "privacy": {key: plist[key] for key in ("NSCameraUsageDescription", "NSLocalNetworkUsageDescription")},
        },
    }],
    "news": [],
}, indent=2, ensure_ascii=False))
