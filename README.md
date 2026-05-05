# Android Photos App - CS213
**Joseph Chavarria & Sarai Ojeda**

Note: If App runs slow in Android Studio, increase the RAM to 4gb or more depending on your computer's specs.

---
# GenAI Usage
**We used GenAI tools for some parts of this project. There was a use of both Claude and Chatgpt and we have elaborated how both were used.**

**What was used for:**
- Generating XML layout files
- Help navigating Android Studio (understanding project structure, Gradle configuration)
- Debugging emulator issues on Windows 11 Home (HAXM not available, WHPX setup)
- Troubleshooting deployment to a physical Samsung S23 Ultra (minSdk mismatch fix)


**Prompts issued:**
- "Auto complete search implementation is not working as intented, how can I fix these issues"
- "Can you help me make the xml files look like the Samsung Gallery app"
- "How do I use AlertDialog in Android to get text input from the user?"
- "I keep trying to run and I get Error running app Pixel 6 is already running as process 18060"


**Code written**
- `Album.java`, `Photo.java`, `Tag.java` from last assignment
- `HomeActivity.java` - full implementation including album create/rename/delete logic, duplicate name checking, persistence calls
- `AlbumAdapter.java` - full RecyclerView adapter with click and long-click listeners
- `StorageUtil.java` - save/load logic using Java serialization
- `TagAdapter`, `PhotoAdapter` - full implementation, bridging between data and UI.
- Connecting all activities together (connections between HomeActivity → AlbumActivity → PhotoDisplayActivity → SearchActivity)
- Updating `HomeActivity.java` to launch `AlbumActivity` and `SearchActivity` via Intent
- Adding all new activities in `AndroidManifest.xml`
- Testing all features on physical device
- Debugged and fixed issues that came up during integration that the AI output did not account for (missing imports, ID mismatches between XML and Java, adapter type errors)

