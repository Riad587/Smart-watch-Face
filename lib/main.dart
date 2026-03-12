import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MaterialApp( debugShowCheckedModeBanner: false, home: WatchStore()));
}

class WatchStore extends StatelessWidget {
  const WatchStore({super.key});
  static const platform = MethodChannel('com.smart_watch/push');

 static const List<WatchFace> watchFaces = [
  WatchFace(name: "Solid Black", image: "assets/images/watch1.png", watchFaceApk: "assets/faces/com.smart_watch.solid_black.apk", packageName: "com.smart_watch.solid_black", token: "VALID)"),
  WatchFace(name: "Classic Retro", image: "assets/images/watch2.png", watchFaceApk: "assets/faces/com.watchfacestudio.retro.apk", packageName: "com.watchfacestudio.retro", token: "VALID"),
];

Future<void> _applyWatchFace({required WatchFace watchFace}) async {
    try {
      // NEW: Request Bluetooth activation before proceeding
      final bool btEnabled = await platform.invokeMethod('requestBluetooth');
      if (!btEnabled) {
        debugPrint("Bluetooth activation denied or failed.");
        return; 
      }

      // 1. Load APK from Assets
      final data = await rootBundle.load(watchFace.watchFaceApk);
      final bytes = data.buffer.asUint8List();

      // 2. Save to Temp Folder (Android needs a physical path)
      final tempDir = await getTemporaryDirectory();
      final tempFile = File("${tempDir.path}/temp_face.apk");
      await tempFile.writeAsBytes(bytes);

      // 3. Trigger Push
      final result = await platform.invokeMethod('pushWatchFace', {
        'path': tempFile.path,
        'packageName': watchFace.packageName,
        'token': watchFace.token,
      });

      debugPrint(result);
    } catch (e) {
      debugPrint("Error: $e");
    }
  }

@override
Widget build(BuildContext context) {
  return Scaffold(
    appBar: AppBar(title: const Text("SmartWatch Face Store")),
    body: GridView.builder(
      padding: const EdgeInsets.all(12),
      itemCount: watchFaces.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
        childAspectRatio: 0.75,
      ),
      itemBuilder: (context, index) {
        final watch = watchFaces[index];

        return Padding(
          padding: const EdgeInsets.all(10),
          child: Column(
            children: [
              Expanded(
                child: Image.asset(
                  watch.image,
                  fit: BoxFit.contain,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                watch.name,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.lightBlue,
                ),
                onPressed: () => _applyWatchFace(watchFace: watch),
                child: const Text("Install", style: TextStyle(color: Colors.white)),
              ),
            ],
          ),
        );
      },
    ),
  );
}
}


class WatchFace {
  final String name;
  final String image;
  final String watchFaceApk;
  final String packageName;
  final String token;

 const WatchFace({required this.name, required this.image, required this.watchFaceApk, required this.packageName, required this.token });
}