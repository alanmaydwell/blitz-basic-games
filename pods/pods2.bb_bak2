;PODS- A Tetrapod type game
;By Alan Maydwell
;March 2002


;go into graphics mode
Graphics 640,480

Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

Global rots=36 ;number of orientations for ship and objects
Global invert=rots/2

Global bounce=False: ;flag used to determine whether ship should rebound


Dim playership(rots) ;stores ship images in different orientations
Dim trig#(rots,1) ;Stores Sin and Cos look-up table.

;enable double buffering
SetBuffer BackBuffer()

;Tells Blitz Basic  to handle centring of images
AutoMidHandle True  


Global startenergy=100


;Type ship parameters
Global player_x#	;x coord
Global player_y#	;y coord
Global player_s#=0	;speed
Global player_dx# ;x velocity
Global player_dy# ;y velocity
Global player_o	;orientation
Global player_e ;energy


;Load Graphics

Global shipimg=LoadImage("data\ship.bmp")

rotate(); Produces rotated images and generates sin/cos lookup table
setship(midx,midy)


While Not KeyDown(1); Outer loop
Cls 

;Draws border to screen with colour that varies with ship coordinates
Color 255,255,0
Rect 0,0,width,height,0
Rect 2,2,(width-4),(height-4),0

controlship()

;Update ship position
player_x#=player_x#+player_dx#
player_y#=player_y#+player_dy#

;Stops ship from leaving edge of screen
bounce=flase
player_x#=confine#(player_x,0,width)
player_y#=confine#(player_y,0,height)
If bounce=True Then player_o=(player_o+invert) Mod(rots):bounce=False;

;Draws the ship
DrawImage playership(player_o),player_x#,player_y#

Text 0,20,"dx "+player_dx# +" dy "+player_dy# +" s "+player_s#

Flip 

Wend 

End 

; Produces rotated images and generates sin/cos lookup table
Function Rotate() 
For loop=0 To rots-1
angle=loop*360/rots
playership(loop)=CopyImage(shipimg)
RotateImage playership(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
;Print loop+" / " + (rots-1) +" Angle, " +angle +" Sin, " +trig#(loop,0) +" Cos, " +trig#(loop,1)
Next
End Function 

;Sets player ship initial conditions
Function setship(x,y)
player_x#=x
player_y#=y
player_dx#=0
player_dy#=0
player_o=0
player_e=startenergy
End Function

;Stearing and accelleration controls for ship
Function controlship()

;If z pressed rotate the ship anticlockwise
If KeyDown(45) Then
player_o=(player_o+1) Mod(rots)
End If

;If x pressed rotate the ship clockwise
If KeyDown(44) Then
player_o=player_o-1
If player_o=-1 Then player_o=rots-1
End If

;if r-shift pressed then accelerate ship
If KeyDown(54) Then
If player_s#<10 player_s#=player_s#+0.50
End If

;Auto slowdown
If player_s#>=0.25 Then player_s#=player_s#-0.25

;Update x and y components of velocity based on new orientation and speed
player_dx#=player_s#*trig#(player_o,0) 
player_dy#=-player_s#*trig#(player_o,1)

End Function

;Stops x# from exceeding min and max values. Also sets bounce flag.
;Used to stop things leaving the edge of the screen
Function confine#(x#,min,max)
If x#<min Then x#=min:bounce=True
If x#>max Then x#=max:bounce=True 
Return x#
End Function