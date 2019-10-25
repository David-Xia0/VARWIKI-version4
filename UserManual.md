# VARWIKI - User Manual 



## Table of contents

<!--ts-->
   * [Launching](#launching)
      *[Set Up](#set-up)
      *[Running Application](#running-application)
<!--te-->


## Launching

### Set Up

Make sure you have a resources folder with api keys and bgm music



### Running Application

Use the attached script file to run the java application.

```bash
source run.sh
```







## Main Menu

![MainMenu](/UserManualImages/MainMenu.png)

First box on the left shows all past creations. They can be replayed In the media player on the box on the right.

New creations can be made with

edited or deleted using the buttons below





Trying to delete a creation will produce a confirmation prompt like below



![MainMenuLocked](/UserManualImages/MainMenu.png)





because this is a childrens learning application. We deem creating videos and deciding what to include in creations to be too complex for a child. So we have introduced a lock. This disabesl and greys out buttons so child wont accidently do somehting bad





The learn Button on the bottom right also leads to the matching quiz game 

## Search Menu

![SearchMenu](/UserManualImages/SearchMenu.png)

here the user is prompted to enter a search term.

the search term must be discoverable on wikipedia otherwise and error message will appear and the user will be prompted for another term.



The user can exit this menu and return to the main menu

## Create Menu



![CreateMenu](/UserManualImages/CreateMenu.png)

All text that is discoverable form wikipedia will be displayed in the box on top. This text can also be directly edited.

creation name can be edited, default name is the search term.

No two creations can have the same name

creations cannot have no name


![CreateMenuVoice](/UserManualImages/CreateMenuVoice.png)



From this text it is possible to test and save audio chunks

Audio chunks are stored in the box

they can be played back deleted

position can also be orderd

Final video will contain audio in the order within the box

![CreateMenuBGM](/UserManualImages/CreateMenuBGM.png)
Background music can also be added to the video

images can be modifyed / next chapeter

or you can select no images




finish creates video with current selection

new search resets the whole screen

cancel returns to main menu nothing is saved

## Image Selection

![ImageSelection](/UserManulImages/ImageSelection.png)



clicking on a image will seleect/deselect.

The video created will back the images selected as the background, with each image evenly sharing time

![ImageNegSelection](/UserManualImages/ImageNegSelection.png)

selecting none deselects all options

select all does the opposite 



## Creation Template





## Matching Game

![MatchingGame](/UserManualImages/MatchingGame.png)



video is played on the left

user can choose one of the four answers on the right

there will always be one correct answer( it is possible that there are multiple correct answers)

the user will be notified by alert message if they are right or wrong

and then a new mathcing question is generated

user can exit at anytime by clicking the back button


