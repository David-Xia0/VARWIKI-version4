# VARWIKI - User Manual 

This application is intended for the educational development of a child (7-10).
Parental Guidence is recommended for the safety and learning effecieny of the child.

## Table of contents

<!--ts-->
   * [Launching](#launching)
      * [Set Up](#set-up)
      * [Running Application](#running-application)
   * [Main Menu](#main-menu)
   * [Search Menu](#search-menu)
   * [Create Menu](#create-menu)
   * [Image Selection](#image-selection)
   * [Creation Template](#creation-template)
   * [Matching Game](#matching-game)
<!--te-->


## Launching

### Set Up
This project was built using the the following operating system and packages. It should preferably be run under the same conditions.
The operating system should be Ubuntu 19.04.

Java Runtime:
Java 13 was used to build this project, and should also be used to run it.


Installed Packages:
  ffmpeg
  soxi
  festival
  wikit
  npm

If you have access to the `softeng206_ubuntu_image_updated.ova` then all required packages should be already installed.


Make sure the following folders and files attatched with this project are in the same directory as the Jar.

`/resources`
`/run.sh`




### Running Application

Use the attached script file to run the java application.

```bash
source run.sh
```







## Main Menu

![MainMenuDelete](/UserManualImages/MainMenuDelete.png)

1) This box shows a list of all past creations. 
2) All Past Creations can be replayed In the media player.
3) Create a new creation
4) Modify a current creation
5) Delete a creation
6) To complete the delete operation, the applicaiton needs confirmation from the user

![MainMenuLocked](/UserManualImages/MainMenuLocked.png)

1) Button locks and unlocks screen
2) Locking screen disables Create,Modify and Delete Buttons

Video Creation, Modification and Deletion should all be done under parental guidence and supervision. Parental guidence is recommended to ensure the childs safety and optimise the learning potential of this application.

3) This button leads to the Matching Quiz [Game](#matching-game)

## Search Menu

![SearchMenu](/UserManualImages/SearchMenu.png)

1) Enter a search term to continue (The search term must be discoverable on wikipedia).
2) returns to main menu

## Create Menu

![CreateMenu](/UserManualImages/CreateMenu.png)

1) All Summary text discovered from Wikipedia will be displayed here. This text can be directly edited.
2) Test text to speech translation of highlighted selection of text within 1)
3) Save text to speech translation fo highlighted selection of text within 1)
4) Save all text within 1) as text to speech audio

![CreateMenuFestival](/UserManualImages/CreateMenuFestival.png)

1) Audio chunks are stored in this list. (The final video created will contain the audio stored in this list according to the vertical order)
2) Selected audio chunk can be played back
3) Selected audio chunk can be deleted
4) Postion of audio chunk can be moved
5) A different text to speech voice package can be selected (the other voice packages may not be able to translate some words)

![CreateMenuBGM](/UserManualImages/CreateMenuBGM.png)

1) Background music can be selected and added to the video
2) Video should be given a name. Default name will be search term entered. Name can only include letters numbers and underscores and hypens.
3) Select if you want your video to contain images obtains from flickr.
4) Select the [specific images](#image-selection) you want in your video
5) Finishes video creation. Creates video then you are returned to main menu when it is done.
6) New Search sends user back to [search screen](#search-menu)
7) Cancel returns user back to [main menu](#main-menu)

## Image Selection

![ImageSelection](/UserManualImages/ImageSelection.png)

1) Clicking on any image within the grid will select or deselect that image

Final Video Created will contain all selected images. Each appear for the same length of time as the other images.

![ImageNegSelection](/UserManualImages/ImageNegSelection.png)

1) Deselects all images
2) Selects all images
3) Closes image selection screen


## Creation Template





## Matching Game

![MatchingGame](/UserManualImages/MatchingGame.png)
 
It is recommeneded that you have a lot of current saved creations. IF you do not have that many, a lot of questions will be repeated
 
1) Video is played here
2) User is meant to choose one of the four answer options (There will always be ATLEAST ONE correct answer)

After the user selects an answer option the following options will appear

1) Go to next question
2) Answer Result
3) Exit the [Main Menu screen](#main-menu)
video is played on the left


