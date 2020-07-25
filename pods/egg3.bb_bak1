;zobulise test

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

Global rots=36 ;number of orientations for ship and objects
Global invert=rots/2
Global rightangle=rots/4

;enable double buffering
SetBuffer BackBuffer()


AutoMidHandle True 

;Load graphics
Global im_bull=LoadImage("data\bull.bmp")
Global im_dia=LoadImage("data\diathing.bmp")
Global im_pod=LoadImage("data\redsq.bmp")
Global im_liz=LoadImage("data\redcro.bmp")
Global im_ship=LoadImage("data\ship.bmp")


;Type ship parameters
Global player_x#	;x coord
Global player_y#	;y coord
Global player_s#=0	;speed
Global player_dx# ;x velocity
Global player_dy# ;y velocity
Global player_o	;orientation
Global player_e ;energy

Global bounce=False: ;flag used to determine whether ship should rebound
Dim playership(rots) ;stores ship images in different orientations
Dim trig#(rots,1) ;Stores Sin and Cos look-up table.


;Type for egg pods and hatched chasers
Type egg
Field x,y
Field dx,dy
Field status
Field targ ;Target: -2=none, -1=player, Whole number=array index of bem
End Type 
npods=5

;Type for bems
Type fly
Field x,y
Field dx,dy
Field status
End Type
Global nfly=2 
Dim bem.fly(nfly)

;bullet type
Type bullet
Field x,y
Field dx#,dy#
Field life#
Field fromwhom
End Type

;Set up various things
rotate()			; Precalculates the ship rotation and creates sin/cos l.u.t.
set_pods(npods) 	;Set up eggs
set_bem				; Set up bems
set_ship(midx,midy) ; Set up ship

;loop until ESC hit...
While Not KeyDown(1)

Cls
update_bem()
update_ship
update_bullets
update_pods
controlship
Flip 
Wend 
End 

;############################
;### Functions Start Here ###
;############################


;Sets player ship initial conditions
Function set_ship(x,y)
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
If KeyDown(45) Then player_o=(player_o+1) Mod(rots)


;If x pressed rotate the ship clockwise
If KeyDown(44) Then
player_o=player_o-1
If player_o=-1 Then player_o=rots-1
End If

;if r-shift pressed then accelerate ship
If KeyDown(54) Then If player_s#<10 player_s#=player_s#+0.50

;Create bullet if firebutton pressed
If KeyHit(28) Then 
pb.bullet=New bullet
pb\x=player_x
pb\y=player_y
pb\dx#=player_dx#+8*trig#(player_o,0)
pb\dy#=player_dy#-8*trig#(player_o,1)
pb\life#=width
End If


;Auto slowdown
If player_s#>=0.25 Then player_s#=player_s#-0.25

;Update x and y components of velocity based on new orientation and speed
player_dx#=player_s#*trig#(player_o,0) 
player_dy#=-player_s#*trig#(player_o,1)
End Function

Function update_ship()

;Plot the ship
DrawImage playership(player_o),player_x#,player_y#

;Update ship position
player_x#=player_x#+player_dx#
player_y#=player_y#+player_dy#

;Stops ship from leaving edge of screen
;Blocks top-edge
If player_y#<0 Then 
;player_y#=0
newori=invert-player_o
If newori<0 Then newori=rots+newori
player_o=newori
End If 
;Blocks botton edge
If player_y#>height Then
;player_y#=height
newori=invert-player_o
If newori<0 Then newori=rots+newori
player_o=newori
End If 
;Blocks left-edge
If player_x#<0 Then 
;player_x#=0
newori=rots-player_o
player_o=newori
End If
;blocks right-edge
If player_x#>width Then 
;player_x#=width
newori=rots-player_o
player_o=newori
End If 

;bounce=flase
;player_x#=confineship#(player_x,0,width)
;player_y#=confineship#(player_y,0,height)
;If bounce=True Then player_o=(player_o+invert) Mod(rots):bounce=False;
End Function 

;Updates bullets
Function update_bullets()
For pb.bullet = Each bullet
DrawImage im_bull,pb\x,pb\y
pb\x=pb\x+pb\dx#
pb\y=pb\y+pb\dy#
pb\life#=pb\life#-(Sqr(pb\dx#^2+pb\dy#^2))

;confine bullets to screen
If pb\x<0 Then pb\x=0:pb\dx#=-pb\dx#
If pb\y<0 Then pb\y=0:pb\dy#=-pb\dy#
If pb\x>width Then pb\x=width:pb\dx#=-pb\dx#
If pb\y>height Then pb\y=height:pb\dy#=-pb\dy#
If pb\life#<=0 Then Delete pb
Next
End Function 


Function set_pods(n)
For loop=0 To n
pod.egg=New egg
pod\x=Rnd(32,width-32)
pod\y=Rnd(32,height-32)
pod\dx=0
pod\dy=0
pod\status=1
pod\targ=-2
Next 
End Function

Function update_pods()
For pod.egg = Each egg

;If in homing mode then home in on target, else don't
If pod\status=-1 Then
DrawImage im_liz,pod\x,pod\y

	If pod\targ=-1 Then 
	pod\dx=homein(pod\x,player_x)
	pod\dy=homein(pod\y,player_y)
	Else 
	i=pod\targ
	pod\dx=homein(pod\x,bem(i)\x)
	pod\dy=homein(pod\y,bem(i)\y)
	End If 


Else DrawImage im_pod,pod\x,pod\y
pod\dx=0:pod\dy=0

End If 

Text pod\x,pod\y,pod\targ

;Stops pods from leaving edge of screen
pod\dx=confine(pod\x,pod\dx,0,width)
pod\dy=confine(pod\y,pod\dy,0,height)

;Update location 
pod\x=pod\x+pod\dx
pod\y=pod\y+pod\dy

;collision detection between pod And bullet

	For pb.bullet = Each bullet
	If ImagesCollide(im_pod,pod\x,pod\y,0,im_bull,pb\x,pb\y,0) Then
	pb\life=0; Delete bullet
	pod\status=-pod\status; Chage status of pod
	If pod\status=-1 Then pod\targ=nearest(pod\x,pod\y);locate target if pod changed to lizard.
	End If 
	Next 

Next
End Function

;Calculates component of velocity so object moves towards target
Function homein(x1,x2)
dx=x1-x2
If dx<0 Then z=1 Else z=-1
Return z
End Function


Function set_bem()
For loop=0 To nfly
bem.fly(loop)=New fly
bem(loop)\x=Rnd(width)
bem(loop)\y=Rnd(height)
Next
End Function

Function update_bem()
For i=0 To nfly
DrawImage im_dia,bem(i)\x,bem(i)\y
Text bem(i)\x,bem(i)\y,i
Next
End Function  

;General Perpose Fuctions

;Generates rotated ship image and creates sin/cos lut
Function Rotate() 
For loop=0 To rots-1
angle=loop*360/rots
playership(loop)=CopyImage(im_ship)
RotateImage playership(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
;Print loop+" / " + (rots-1) +" Angle, " +angle +" Sin, " +trig#(loop,0) +" Cos, " +trig#(loop,1)
Next

;For loop=o To rots-1
;dx=200*trig#(loop,0)
;dy=200*trig#(loop,1)
;Line midx,midy,midx+dx,midy+dy
;Text midx+dx,midy+dy,loop
;Next
;Flip
;WaitKey

End Function 

Function confine(x,dx,min,max)
If x<min And dx<0 Then dx=-dx
If x>max And dx>0 Then dx=-dx
Return dx
End Function

;Determines nearest object (either ship or fly)
Function nearest(x,y)

;Assumes player is nearest and sets target to player
target=-1
sep=dist(x,y,player_x,player_y)


;Checks to see if any of the bems are closer than the player
For i=0 To nfly
j=dist(x,y,bem(i)\x,bem(i)\y)
If j<sep Then
sep=j 
target=i
End If 
Next

Return target
End Function

;Calculates distance between two points
Function dist(x1,y1,x2,y2)
dx=x1-x2
dy=y1-y2
dist=Sqr(dx*dx+dy*dy)
Return dist 
End Function 