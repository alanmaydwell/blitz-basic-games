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

;##################
;# Ship Parameters#
;##################
Global player_x#	;x coord
Global player_y#	;y coord
Global player_s#=0	;speed
Global player_dx# ;x velocity
Global player_dy# ;y velocity
Global player_o	;orientation
Global player_e ;energy
Global control=0;AP is able to control motion of ship if this value is zero.

;width and height of ship image
Global shipwidth=ImageWidth(im_ship)
Global shipheight=ImageHeight(im_ship)

;x and y boundaries for ship movement
Global shipxmax=width-(shipwidth/2)
Global shipxmin=shipwidth/2
Global shipymax=height-(shipheight/2)
Global shipymin=shipheight/2

Dim playership(rots) ;stores ship images in different orientations
Dim trig#(rots,1) ;Stores Sin and Cos look-up table.

;#######################################
;#Egg pod and hatched chaser parameters#
;#######################################
Type egg
Field x,y
Field dx,dy
Field status
Field targ ;Target: -2=none, -1=player, Whole number=array index of bem
End Type
Global et=0; "Egg timer" time quantisation interval for homing things 
Global npods=12
Dim pod.egg(npods)

;Pod animated image parameters and creation
Global pn=12				;number of frames in animation (counting from 1, although when plotting count runs from 0 to n-1)
frame#=width/32	;width of each frame (based on screen width)
rad#=frame/2	;maximum radius of circle in each frame
dec#=frame/pn    ;decriment for making circles of smaller radius
Global im_circs=CreateImage(frame#,frame#,pn); The images are stored here
Global pframe=0; current frame number in pod animation


;width and height of pod image
Global podwidth=frame#
Global podheight=frame#

;x and y boundaries for pod/chaser movement
Global podxmax=width-(podwidth/2)
Global podxmin=podwidth/2
Global podymax=height-(podheight/2)
Global podymin=podheight/2

;generates the concentric circle animation for pods
loop=1
Repeat
SetBuffer ImageBuffer(im_circs,loop-1) 
Color 255,0,255
Oval 0,0,frame,frame,0
Color 0,0,255
k=loop-1
j=frame#-(loop*dec#)
Oval rad-j/2,rad-j/2,j,j,0
loop=loop+1
Until loop>pn
SetBuffer BackBuffer()
Color 255,255,255


 
;##################
;# BEM Parameters #
;##################
Type fly
Field x,y
Field dx,dy
Field status
Field t; timer (when timer reaches 0 then bem changes direction)
Field tp; type of bem
Field sp; %age probability of shooting per screen update
End Type
Global nfly=4 
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
set_bem(nfly)				; Set up bems
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

;If player able to control motion ship (control=0) then 
;Update x and y components of velocity based on new orientation and speed
If control=0 Then 
player_dx#=player_s#*trig#(player_o,0) 
player_dy#=-player_s#*trig#(player_o,1)
Else control=control -1
End If 

End Function

Function update_ship()

;Plot the ship
DrawImage playership(player_o),player_x#,player_y#

;Update ship position
player_x#=player_x#+player_dx#
player_y#=player_y#+player_dy#

;Stops ship from leaving edge of screen
;Blocks top-edge
If player_y#<shipymin Then 
player_y#=shipymin
newori=invert-player_o
If newori<0 Then newori=rots+newori
player_o=newori
End If 
;Blocks botton edge
If player_y#>shipymax Then
player_y#=shipymax
newori=invert-player_o
If newori<0 Then newori=rots+newori
player_o=newori
End If 
;Blocks left-edge
If player_x#<shipxmin Then 
player_x#=shipxmin
newori=rots-player_o
player_o=newori
End If
;blocks right-edge
If player_x#>shipxmax Then 
player_x#=shipxmax
newori=rots-player_o
player_o=newori
End If 

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
For i=0 To n
pod.egg(i)=New egg
pod(i)\x=Rnd(32,width-32)
pod(i)\y=Rnd(32,height-32)
pod(i)\dx=0
pod(i)\dy=0
pod(i)\status=1
pod(i)\targ=-1
Next 
End Function

Function update_pods()
;update animation frame
pframe=pframe+1
If pframe=pn Then pframe=0 
For i=0 To npods

;If in homing mode then home in on target, else don't

et=et-1:If et<0 Then et=30; Countdown timer for direction update for chasers

If pod(i)\status=-1 Then
DrawImage im_liz,pod(i)\x,pod(i)\y

	If et=0
	If pod(i)\targ=-1 Then 
	pod(i)\dx=homein(pod(i)\x,player_x)
	pod(i)\dy=homein(pod(i)\y,player_y)
	Else 
	j=pod(i)\targ
	pod(i)\dx=homein(pod(i)\x,bem(j)\x)
	pod(i)\dy=homein(pod(i)\y,bem(j)\y)
	End If 
    End If 

Else DrawImage im_circs,pod(i)\x,pod(i)\y,pframe
End If 

;Text pod(i)\x,pod(i)\y,pod(i)\targ

;Stops pods from leaving edge of screen
pod(i)\dx=confine(pod(i)\x,pod(i)\dx,podxmin,podxmax)
pod(i)\dy=confine(pod(i)\y,pod(i)\dy,podymin,podymax)

;Update location 
pod(i)\x=pod(i)\x+pod(i)\dx
pod(i)\y=pod(i)\y+pod(i)\dy


;collision detection between pod and ship
If ImagesCollide(im_pod,pod(i)\x,pod(i)\y,0,playership(player_o),player_x#,player_y#,0)Then 
a1=pod(i)\dx
a2=player_dx#
b1=pod(i)\dy
b2=player_dy#

player_dx#=a1
pod(i)\dx=a2
player_dy#=b1
pod(i)\dy=b2

control=30; Prevents player from being able to change ship's velocity for a number of loops.

End If 

;collision detection between pod And bullet

	For pb.bullet = Each bullet
	If ImagesCollide(im_pod,pod(i)\x,pod(i)\y,0,im_bull,pb\x,pb\y,0) Then
	pb\life=0; Delete bullet
	pod(i)\status=-pod(i)\status; Chage status of pod
	If pod(i)\status=-1 Then pod(i)\targ=nearest(pod(i)\x,pod(i)\y);locate target if pod changed to lizard.
	End If 
	Next; (bullet)
	
	
	;detects collisions between pods
	;Causes sort of elastic collisons
	For j=i To npods
	If j<>i And ImagesCollide(im_pod,pod(i)\x,pod(i)\y,0,im_pod,pod(j)\x,pod(j)\y,0) Then
    a1=pod(i)\dx
	a2=pod(j)\dx
	b1=pod(i)\dy
	b2=pod(j)\dy
	pod(j)\dx=a1
	pod(i)\dx=a2
	pod(j)\dy=b1
	pod(i)\dy=b2
	End If 
	Next ;(j)

Next; (i)

End Function

;Calculates component of velocity so object moves towards target
Function homein(x1,x2)
dx=x1-x2
If dx<0 Then z=1 Else z=-1
Return z
End Function


Function set_bem(n)
For i=0 To n
Print i:Flip 
bem.fly(i)=New fly
bem(i)\x=Rnd(width)
bem(i)\y=Rnd(height)
bem(i)\t=50
bem(i)\dx=bem_speed()
bem(i)\dy=bem_speed()
Next
End Function

;Generates a random number centred on 0 but not = 0
Function bem_speed()
Repeat
x=Rnd(-1,1)
Until x<>0
Return x
End Function 

Function update_bem()
For i=0 To nfly
DrawImage im_dia,bem(i)\x,bem(i)\y
;Text bem(i)\x,bem(i)\y,i

;Confine to screen
bem(i)\dx=confine(bem(i)\x,bem(i)\dx,0,width)
bem(i)\dy=confine(bem(i)\y,bem(i)\dy,0,height)

;Update location
bem(i)\x=bem(i)\x+bem(i)\dx
bem(i)\y=bem(i)\y+bem(i)\dy

;Caused bem to change direction when timer=0
bem(i)\t=bem(i)\t-1
If bem(i)\t=0 Then
bem(i)\t=20+Rnd(30)
bem(i)\dx=bem_speed()
bem(i)\dy=bem_speed()
End If 

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