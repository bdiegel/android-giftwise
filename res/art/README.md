## Working on the icon artwork


### Explanation of files

    ic_launcher.blend         Blender source artwork. Work begins here.
    ic_launcher-render.png    Blender render output saved to a PNG
    ic_launcher.xcf           Gimp file made from the above PNG
    ic_launcher.png           Final output from Gimp
    ic_launcher.zip           Artifacts made with the Android Asset Studio


### How to make the launcher icon artwork

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


Now see the section below on __Deploying Into The Project Dir__


### How to make the gray icon artwork

Make the Android icons with Android Asset Studio's Launcher Icon
Generator

   * Load the [Generic Icon Generator](https://romannurik.github.io/AndroidAssetStudio/icons-generic.html) in a browser
   * In `Source`, click `IMAGE` and pick the `ic_launcher.png`
     file we made above.
   * Use these settings:
      * Source TRIM: TRIM
      * Source PADDING: 0%
      * Size: 48dp
      * Padding: 4dp
      * Color: #dadada
      * Name: gift_silhouette_48dp
   * Confirm that the artwork looks amazing
   * Click the `DOWNLOAD .ZIP` button, save the file alongside the
     other files (it gets committed to source control as well)


### Deploying Into The Project Dir

The next step is to deploy the files in this ZIP to their proper
locations in the repository. Inside the ZIP, they're almost in the
right directory structure already, we can unpack in-place like this
(starting in the project root dir):

    $ pushd app/src/main/
    $ unzip /path/to/zipfile

If this was the Launcher Icon, do this as well:

    $ mv web_hi_res_512.png ../../../res/prod/

And then back out and commit:

    $ popd
    $ git commit ...
