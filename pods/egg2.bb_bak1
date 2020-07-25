;zobulise test

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

Global rots=36 ;number of orientations for ship and objects
Global invert=rots/2

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



Type egg
Field x,y
Field dx,dy
Field status
Field targ ;Target: -2=none, -1=player, Whole number=array index of bem
End Type 
npods=5

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

rotate()
set_pods(npods)
set_bem
set_ship(midx,midy)

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

If KeyHit(28) Then 
pb.bullet=New bullet
pb\x=player_x
pb\y=player_y
pb\dx#=player_dx#+8*trig#(player_o,0)
pb\dy#=player_dy#-8*trig#(player_o,1)
pb\life#=400
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
bounce=flase
player_x#=confineship#(player_x,0,width)
player_y#=confineship#(player_y,0,height)
If bounce=True Then player_o=(player_o+invert) Mod(rots):bounce=False;
End Function 

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
pod\dx=Rnd(-2,2)
pod\dy=Rnd(-2,2)
pod\status=1
pod\targ=-2
Next 
End Function

Function update_pods()
For pod.egg = Each egg
If pod\status=-1 Then
DrawImage im_liz,pod\x,pod\y
Else DrawImage im_pod,pod\x,pod\y
End If 

Text pod\x,pod\y,pod\targ


pod\dx=confine(pod\x,pod\dx,0,width)
pod\dy=confine(pod\y,pod\dy,0,height)

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

Function Rotate() 
For loop=0 To rots-1
angle=loop*360/rots
playership(loop)=CopyImage(im_ship)
RotateImage playership(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
;Print loop+" / " + (rots-1) +" Angle, " +angle +" Sin, " +trig#(loop,0) +" Cos, " +trig#(loop,1)
Next
End Function 

Function confine(x,dx,min,max)
If x<min And dx<0 Then dx=-dx
If x>max And dx>0 Then dx=-dx
Return dx
End Function

;Stops x# from exceeding min and max values. Also sets bounce flag.
;Used to stop things leaving the edge of the screen
Function confineship#(x#,min,max)
If x#<min Then x#=min:bounce=True
If x#>max Then x#=max:bounce=True 
Return x#
End Function


;Determines nearest fly to specified coordinates
Function nearest(x,y)
target=-2:sep=10000

;Checks to find the nearest bem
For i=0 To nfly
j=dist(x,y,bem(i)\x,bem(i)\y)
If j<sep Then
sep=j 
target=i
End If 
Next

;checks to see if player is nearer. If he is then set him as target (-1)
j=dist(x,y,player_x,player_y)
If j<sep Then target=-1

Return target
End Function

;Calculates distance between two points
Function dist(x1,y1,x2,y2)
dx=x1-x2
dy=y1-y2
dist=Sqr(dx*dx+dy*dy)
Return dist 
End Function 