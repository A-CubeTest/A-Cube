# RePlay #
RePlay is a system made up by an Accessibility Service and a configuration app who improve videogames's accessibility on mobile devices. The old name of this system was *A-Cube*, much of the code still uses this old name.  
The idea is to be able to simulate the action that the user intends to do by intercepting this what does it do.
In order to generate an event starting from the user's action, it is necessary to know two things:
* know what are the interaction tools that the user uses (peripherals external input or voice commands) and what are the actions it can perform with such tools
* know which are the events of the games that it is possible to generate (meaning where on the screen is it possible to have an interaction with the graphic elements).      

## A-Cube (Adaptive Android Assistant) ## <img src="img/a_cube_logo.png" width=150>  
A-Cube it's an expansion of the QAAD project and it's composed by an Accessibility Service (*AccessibilityService_A-Cube*) and a configuration app (*A-Cube*).  
The A-Cube configuration app stores the objects we're going to describe below in the Downloads folder of the mobile device where it's installed.
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
  * **Action name**, reference to a registered action.  
  * **Event name**, reference to a registered event.    
  * **Marker size**, size of the marker used to define the point on the screen where the user wants the event to be executed the event.      
  * **Marker color**, color of the marker just mentioned above.  
  
### SVM Models ### 
SVM Models are references to models created using the Support Vector Machines (SVM) classifier. These models allow the recognition of vocal commands. They are composed by:  
  * **Name**, uniquely identifies the SVM Models inside A-Cube.  
  * **Sounds**, names of the Vocal Actions that the model is capable of recognize.  
  
### Configurations ###  
Configurations are containers that serve to group Links according to a logical scheme. For example we can create two Configurations for the same game: the first containing
Voice actions and the second containing Button actions that activate both the same Events. They are composed by:  
  * **Name**, is the name of the Configuration, two Configurations cannot exist with the same name associated with the same game.   
  * **Bundle-ID**,indicates which game the Configuration refers to.
  * **SVM Model**, SVM Model name used in this Configuration.    
  * **Selected**, value that indicates which Configuration it is active.  
  * **Links**, the list of Links contained in this Configuration.  
  
# Instructions for testing the code #
