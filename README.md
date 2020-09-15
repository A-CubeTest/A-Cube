# QAAD (Quick Access to Android Device)
QAAD is an Accessibility Service who improve videogames's accessibility on mobile devices. 
The idea is to be able to simulate the action that the user intends to do by intercepting this what does it do.
In order to generate an event starting from the user's action, it is necessary to know two things:
* know what are the interaction tools that the user uses (peripherals external input or voice commands) and what are the actions it can perform with such tools
* know which are the events of the games that it is possible to generate (meaning where on the screen is it possible to have an interaction with the graphic elements).

Once these two information are known, the association between the action of the user and the event that can be made to happen in the video game. QAAD needs two main components to work:  
**1. QAAD Configuration**  
It allows you to record external input devices and commands to be stored, as well as voice commands for voice interaction.  
In addition to storing user actions, QAAD Configuration allows you to associate them with the events that can be created with the installed video games. It stores the list of available video games and for each of them they come saved events.  
**2. QAAD Tap Position**  
In order to trigger the events in the video game associated with the player's actions, it's necessary to know the coordinates of the elements that make up the screen. What QAAD Tap Position need to do is use the screenshots of the video game screens, you need to click on a specific point to derive its coordinates, which are subsequently used to manually update the information contained in the configuration file of video games.    

# A-Cube (Adaptive Android Assistant) <img src="img/a_cube_logo.png" width=150>  
A-Cube it's an expansion of the QAAD project and it's composed by an Accessibility Service (*AccessibilityService_A-Cube*) and a configuration app (*A-Cube*).  
The main data structures are **Actions**, **Events**, **Games**, **Links**, **SVM Models** and **Configurations**.  
### Actions ###
Actions are the input commands through which it is possible activate Events, they are composed by:  
  * **Name**, the name who identify the specific action (for example Vocal A or Button B).  
  * **Type**, it specify the action type that can be *Button* or *Vocal*.   

### Events ###
Events are objects capable of emulating interactions with the touch screen of mobile devices, they are composed by:
  * **Name**, identifies uniquely the Event with respect to the game it is associated with.  
  * **Type**, represents the type of gesture to emulate.
  * **Coordinates**, indicate where on the screen the Event will be performed.  
  * **Screenshot**, represents the game screen where we want to run the Event.  
  * **Portrait**, indicates whether you are playing the game in Portrait or Landscape.  
  
### Games ###
Games are references to games installed inside our device, they are composed by:  
  * **Bundle-id**, name of the game package that identifies it uniquely within A-Cube.  
  * **Title**, game title, is shown inside A-Cube to identify the game.  
  * **Icon**, game icon, is shown inside A-Cube to identify the game.   
  * **Events**, the list of events associated with this game.  

### Links ###
Links are objects that relate Actions to Events. When the Action is performed, the defined gesture is generated from the associated event. They are composed by:  
  * **Action name**    
  * **Event name**  
  * **Marker size**    
  * **Marker color**  
