# Melanoma detection on a smartphone

This android application analyzes a lesion image and can classify it as benign or suspicious of melanoma. It is developed using computer vision library OpenCV 3.2 and Java in Android Studio.

## Requirments

### Environment Setup

- Download & Install [Android Studio]

- Download and extract [OpenCV Android Pack] folder and then set `OPENCV_ANDROID_SDK` environment variable pointing to the downloaded folder location

- Clone this repository and copy necessary files

```cmd
  git clone https://github.com/ukalwa/melanoma-detection
  mkdir jniLibs
  cd melanoma-detection\app\src\main\jniLibs
  REM copy the libraries from opencv to this location
  xcopy /E %OPENCV_ANDROID_SDK%\sdk\native\libs .
```

- Now open Android Studio and select *Open an existing Android Studio Project*

- Navigate to the melanoma-detection directory and select it

- When it prompts to install necessary tools, select OK.

- After all the necessary tools are installed, the project is ready to be tested.

- Click `Run` and connect to an Android device or run an emulator (*x86 preferred*) and give required permissions.

- Install OpenCV_manager when it asks and if you get an error, uninstall it and install the apk from `%OPENCV_ANDROID_SDK%\apk`

```cmd
  REM for x86 device
  adb shell %OPENCV_ANDROID_SDK%\apk\OpenCV_3.2.0_Manager_3.20_x86.apk
```

## Steps involved

The code performs following steps:

1. Reads in dermoscopic lesion image by taking image from the smartphone camera coupled with a [10x lens] or reads an image from the SD card.
2. Preprocess the image by applying color transformation and filtering
3. Segment the lesion from the image using active contour model
4. Extract features (*Asymmetry, Border irregularity, Colors, Diameter*) from the lesion
5. Classify the lesion based on the features extracted using an SVM classifier and output the result.
6. Save the processed images and results

## License

This code is GNU GENERAL PUBLIC LICENSED.

## Contributing

If you have any suggestions or identified bugs please feel free to post them!

## Acknowledgements

- Active contour segmentation is adapted from [ofeli] project
- The computer vision library used for this project is [OpenCV]

[OpenCV Android Pack]: https://opencv.org/releases.html
[Android Studio]: https://developer.android.com/studio/
[10x lens]: https://www.amazon.com/AMIR-180°Fisheye-Screwed-Together-Smartphones/dp/B0179JX8GC
[ofeli]: https://github.com/pkuwwt/ofeli
[OpenCV]: https://opencv.org/releases.html