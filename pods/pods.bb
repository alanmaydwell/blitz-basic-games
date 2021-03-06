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
If player_x#<0 Then player_x#=0: player_dx#=-player_dx#
If player_x#>width Then player_x#=width: player_dx#=-player_dx#
If player_y#<0 Then player_y#=0: player_dy#=-player_dy#
If player_y#>height Then player_y#=height: player_dy#=-player_dy#

;Draws the ship
DrawImage playership(player_o),player_x#,player_y#

Text 0,20,"dx "+player_dx# +" dy "+player_dy#

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
player_o=player_o+1 
If player_o=rots Then player_o=0
End If

;If x pressed rotate the ship clockwise
If KeyDown(44) Then
player_o=player_o-1
If player_o=-1 Then player_o=rots-1
End If

;if r-shift pressed then accelerate ship
If KeyDown(54) Then
player_dx#=player_dx#+(.1*trig#(player_o,0)) 
player_dy#=player_dy#-(.1*trig#(player_o,1))
End If

End Function 