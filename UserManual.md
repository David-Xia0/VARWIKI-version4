# VARWIKI - User Manual 

This application is intended for the educational development of children (7-10).
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
   * [Media Player](#media-player)
   * [Matching Game](#matching-game)
<!--te-->


## Launching

### Set Up
This project was built using the the following operating system and packages. It should preferably be run under the same conditions.

The operating system is L:inux Ubuntu 19.04.

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


![MainMenuLocked](/UserManualImages/MainMenuLocked.png)


1) This box shows a list of all past creations. 
2) All Past Creations can be replayed In the [media player](#media-player).
3) Button locks and unlocks screen
4) These options are greyed out. Will become funcitonal if screen is unlocked


Video Creation, Modification and Deletion should all be done under parental guidence and supervision. Parental guidence is recommended to ensure the childs safety and optimise the learning potential of this application.

5) This button leads to the Matching Quiz [Game](#matching-game)

![MainMenuDelete](/UserManualImages/MainMenu.png)

This is an exmaple of the screen Unlocked

1) Create a new creation
2) Modify a current creation
3) Delete a creation
4) To complete the delete operation, the applicaiton needs confirmation from the user



## Search Menu

![SearchMenu](/UserManualImages/SearchMenu.png)

1) Enter a search term to continue (The search term must be discoverable on wikipedia).
2) returns to main menu

## Create Menu

![](/UserManualImages/CreateHub.png)

1) Name of the Video Creation can be edited. Default name is creation topic
2) [Modify Audio](#modify-audio)
3) [Image Selection](#image-selection)
4) Include Images or not
5) Select Background Music
6) Finishes video creation. Creates video then you are returned to main menu when it is done.
7) New Search sends user back to [search screen](#search-menu)
8) Cancel returns user back to [main menu](#main-menu)


## Modify Audio

![](/UserManualImages/ModifyAudio.png)

1) Audio chunks are stored in this list. (The final video created will contain the audio stored in this list according to the vertical order)
2) Selected audio chunk can be played back
3) Selected audio chunk can be deleted
4) Postion of audio chunk can be moved up or down
5) [Create New Audio Chunks](#create-audio)
6) return to [Create Menu](#create-menu)


## Create Audio

![](/UserManualImages/CreateAudio.png)

1) text obtained from wikipedia is stored here
2) Select the voice package used in text to speech translation of text
3) Test the text to speech translation of highlighted text
4) Save Text to speech translation of highlighted text
5) Save all Text for Text to speech translation
6) return to [Modify Audio Menu](#modify-audio)



## Image Selection

![ImageSelection](/UserManualImages/SelectImages.png)

1) Clicking on any image within the grid will select or deselect that image

1) Deselects all images
2) Selects all images
3) Closes image selection screen

## Matching Game

![MatchingGame](/UserManualImages/MatchingGame.png)
 
It is recommeneded that you have a lot of current saved creations. IF you do not have that many, a lot of questions will be repeated
 
1) Video is played here
2) User is meant to choose one of the four answer options (There will always be ATLEAST ONE correct answer)


1) Go to next question
2) Answer Result
3) Exit the [Main Menu screen](#main-menu)
video is played on the left

## Media Player

![MatchingGame](/UserManualImages/MediaPlayer.png)


1) Clicking on the video plays or pauses it.
2) Current duration played
3) Go to start of Video/Go to previous Video
4) Go to next Video
5) Video Time slider
6) Mute/Unmute



