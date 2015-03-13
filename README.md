## Explanation of files

`ic_launcher.blend`

> Blender source artwork. Work begins here.


`ic_launcher-render.png`

> Blender render output saved to a PNG for post-processing in Gimp


`ic_launcher.xcf`

> Gimp file where post-processing is performed, made from the above PNG


`ic_launcher.png`

> Final output from Gimp to be used in Android Asset Studio


`ic_launcher.zip`

> Artifacts made with the Android Asset Studio Launch Icon tool. These are to be packed into the Android APK.


## How to make the icon artwork

First, we need to render the scene to a still PNG image

   * In Blender, perform a render with `F12`
   * Save the render still image. `Image menu -> Save As Image`,
     save over `ic_launcher-render.png` (NOT `ic_launcher.png`)
   * Close the render window with `ESC`


Next, we need to set the background region of the image to transparent

   * In Gimp, open `ic_launcher-render.png`
   * `Select menu -> By Color`
   * Click in the gray background region, you should see the
     background and foreground color only selected.
   * Press `Delete` and it should change to checkered transparent color


And then save the real PNG for Android along with the XCF work from Gimp

   * `File menu -> Export As...` and choose to overwrite
     `ic_launcher.png`. Use the PNG default values in the dialog. This
     is the image for Android.
   * `File menu -> Save As...` and choose to overwrite
     `ic_launcher.xcf`. This is just to save the work done in Gimp.


Next, make the Android icons with Android Asset Studio's Launcher
Icon Generator

   * Load the [Launcher Icon Generator](https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html) in a browser
   * In `Foreground`, click `IMAGE` and pick the `ic_launcher.png`
     file we made above.
   * Under `Shape`, click `NONE`
   * Click the `GENERATE WEB ICON` button
   * Confirm that all of the artwork looks amazing
   * Click the `DOWNLOAD .ZIP` button, save the file alongside the
     other files (it gets committed to source control as well)


That's it, the contents of the zip file are suitable for inclusion in your Android project.
