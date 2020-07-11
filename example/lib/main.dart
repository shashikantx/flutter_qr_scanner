import 'package:flutter/material.dart';
import 'package:flutter_qr_scanner/qr_scanner_camera.dart';
import 'package:flutter_qr_scanner/camera_lens_direction.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter QR/Bar Code Reader',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter QR/Bar Code Reader'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String _qrInfo = 'Scan a QR/Bar code';
  bool _camState = false;
  bool _isBackLens = true;
  QrReaderController _qrReaderController = QrReaderController();

  _qrCallback(String code) {
    setState(() {
      _camState = false;
      _qrInfo = code;
    });
  }

  _scanCode() {
    setState(() {
      _camState = true;
    });
  }

  @override
  void initState() {
    super.initState();
    _scanCode();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: Text(widget.title),
        actions: [
          Switch(
              activeTrackColor: Colors.white,
              value: _isBackLens,
              onChanged: (val) async {
                setState(() {
                  _qrReaderController.start();
                });
                setState(() {
                  _isBackLens = !_isBackLens;
                });
                setState(() {
                  _qrReaderController.restart();
                });
              })
        ],
      ),
      body: _camState
          ? Center(
              child: SizedBox(
                height: 1000,
                width: 500,
                child: QRScannerCamera(
                  onError: (context, error) => Text(
                    error.toString(),
                    style: TextStyle(color: Colors.red),
                  ),
                  qrCodeCallback: (code) {
                    _qrCallback(code);
                  },
                  cameraLensDirection: _isBackLens
                      ? CameraLensDirection.back
                      : CameraLensDirection.front,
                  controller: _qrReaderController,
                ),
              ),
            )
          : Center(
              child: Text(_qrInfo),
            ),
    );
  }
}
