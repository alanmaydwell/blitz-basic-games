;zobulise test

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2
;Sets some horizontal coordinates and stores in H(n)
steps#=20
Dim H(steps#)
For loop#=1.00 To steps#
H(loop#)=(loop#/steps#)*width
Next
;Sets some vertical coordinates and stores in V(n)
steps#=20
Dim V(steps#)
For loop#=1.00 To steps#
V(loop#)=(loop#/steps#)*height
Next


Global rots=36 ;number of orientations for ship and objects
Global invert=rots/2
Global rightangle=rots/4

;enable double buffering
SetBuffer BackBuffer()


AutoMidHandle True 

;Load graphics
Global im_bull1=LoadImage("data\bull.bmp")
Global im_bull2=LoadImage("data\bull2.bmp")
Global im_bem1=LoadImage("data\diathing.bmp")
Global im_bem2=LoadImage("data\redcross1.bmp")
Global im_pod=LoadImage("data\redsq.bmp")
Global im_liz=LoadImage("data\redcro.bmp")
Global im_ship=LoadImage("data\ship.bmp")

;load sounds
Global sn_exp=LoadSound("data\Arilou-Warp.wav")
Global sn_pod=LoadSound("data\Chenjesu-Dogi.wav")
Global sn_las=LoadSound("data\Laser01.mp3")

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
Global maxpods=12
Dim pod.egg(maxpods)

;Pod animated image parameters and creation
Global pn=12				;number of frames in animation (counting from 1, although when plotting count runs from 0 to n-1)
Global frame#=width/32	;width of each frame (based on screen width)
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

;Generates the animated image
make_pod_image()

;Chaser animated image parameters and set up
Global nw=8 ; number of animation frames for chaser (nw= number of wiggles)
Global im_home1=CreateImage(frame#,frame#,nw+1)
Global im_home2=CreateImage(frame#,frame#,nw+1)
Global cframe=0; frame in chaser animation
 
make_chaser_image() 

Color 255,255,255

 
;##################
;# BEM Parameters #
;##################
Type fly
Field x,y
Field dx,dy
Field t; timer (when timer reaches 0 then bem changes direction)
Field tp; type of bem (0 indicates dead)
Field sp; per 1000 probability of shooting per screen update
End Type
Global maxbem=32 
Dim bem.fly(maxbem)

;bullet type
Type bullet
Field x,y
Field dx#,dy#
Field life#
Field fw; from whom (1=ship,2=baddie)
End Type

;Explosiong thing type 
Type zob
Field count
Field max
Field x1,y1
Field x2,y2
Field x3,y3
Field x4,y4
Field dx,dy
End Type


;###################
;# Game Parameters #
;###################
Global lives=3
Global level=1
Global bemcount=0 ;Tracks number of alive BEMS
Global pbcount=0  ;Tracks number of player bullets
Global pbmax=4	  ;Maximum numbe of player bullets 
Global npods=12    ;Number of pods. Cannot exceed value of maxpods
Global nbem=1     ;Number of BEMS. Cannot exceed value of maxbem
Global xmax=H(13) ;Set boundaries of zone where only ship can appear at start of level
Global xmin=H(7)
Global ymax=V(13)
Global ymin=V(7)

;Check size of zone
;Line xmin,ymin,xmax,ymin
;Color 255,0,0
;Line xmin,ymax,xmax,ymax
;WaitKey


;Set up various things
rotate()			; Precalculates the ship rotation and creates sin/cos l.u.t.

While Not KeyDown(1) 
set_pods(npods) 	;Set up eggs
set_bem(nbem)				; Set up bems
bemcount=nbem
set_ship(midx,midy) ; Set up ship

;loop until ESC hit...
While Not KeyDown(1) Or bemcount=0

Cls
update_bem()
update_ship
update_bullets
Text 100,100,pbcount+"  "+bemcount

update_pods
plotsqr() 
controlship
Flip 
Wend
If nbem<maxbem Then nbem=nbem+1
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
If KeyDown(54) Then If player_s#<5 player_s#=player_s#+0.50

;Create bullet if firebutton pressed and maximum number of bullets not reached
If KeyHit(28) And pbcount<pbmax Then
PlaySound sn_las
pb.bullet=New bullet
pb\x=player_x
pb\y=player_y
pb\dx#=player_dx#+8*trig#(player_o,0)
pb\dy#=player_dy#-8*trig#(player_o,1)
pb\life#=width
pb\fw=1
End If


;Auto slowdown
If player_s#>=0.15 Then player_s#=player_s#-0.15

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
pbcount=0
For pb.bullet = Each bullet
If pb\fw=1 DrawImage im_bull1,pb\x,pb\y:pbcount=pbcount+1
If pb\fw=2 DrawImage im_bull2,pb\x,pb\y
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

Function make_pod_image()
;generates the concentric circle animation for pods
rad#=frame#/2	 ;maximum radius of circle in each frame
dec#=frame/pn    ;decriment for making circles of smaller radius
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
End Function 

Function make_chaser_image()
loop=0
pmax=720
peek=frame#/10

;Make animation frames for chaser
Repeat

p=0
	Repeat
	;Create diagonal line with superimposed sin wave with aplitude varying from frame to fram.
	;Two versions created, one the mirror image of the other
	If loop<nw/2 Then amp=loop*peek Else amp=(nw-loop)*peek 
	
	x=(p*frame#/pmax)+Sin(p*2)*amp
	y=p*frame#/pmax
	SetBuffer ImageBuffer(im_home1,loop)
	Plot x,y
	SetBuffer ImageBuffer(im_home2,loop)
	Plot frame#-x,y
	Color 0,255,50+Rnd(205)
	p=p+1
	Until p>pmax
loop=loop+1
Until loop>nw
SetBuffer BackBuffer()
End Function 

Function set_pods(n)
For i=0 To n
pod.egg(i)=New egg

;Chooses random location but outside excluded area
Repeat 
pod(i)\x=Rnd(32,width-32)
pod(i)\y=Rnd(32,height-32)
Until pod(i)\x>xmax Or pod(i)\x<xmin Or pod(i)\y>ymax Or pod(i)\y<ymin

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

;Display the chaser image based on direction it is moving.
If pod(i)\dx=pod(i)\dy Then 
DrawImage im_home1,pod(i)\x,pod(i)\y,cframe
Else DrawImage im_home2,pod(i)\x,pod(i)\y,cframe
End If 


cframe=cframe+1:If cframe>nw Then cframe=0;advance animation frame For chaser

	If et=0 Then; Change direction timer (et) reaches 0 then update direction 
	If pod(i)\targ=-1 Then 
	pod(i)\dx=homein(pod(i)\x,player_x)
	pod(i)\dy=homein(pod(i)\y,player_y)
	Else 
	j=pod(i)\targ
	pod(i)\dx=homein(pod(i)\x,bem(j)\x)
	pod(i)\dy=homein(pod(i)\y,bem(j)\y)
	If bem(j)\tp=0 Then pod(i)\status=1; If someome killed the target then change back to egg
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

player_x#=player_x#+player_dx#
player_y#=player_y#+player_dy#

control=30; Prevents player from being able to change ship's velocity for a number of loops.

End If 

;collision detection between pod And bullet

	For pb.bullet = Each bullet
	If ImagesCollide(im_pod,pod(i)\x,pod(i)\y,0,im_bull1,pb\x,pb\y,0) Then
	
		If pb\fw=1 Then 
		pb\life=0; Delete bullet
		pod(i)\status=-pod(i)\status; Chage status of pod
		PlaySound sn_pod
		End If 
	
		If pb\fw=2 Then 
		pod(i)\status=1;BEM bullets can only change chaser into pod but not visa versa.
		pb\dx=-pb\dx:pb\dy=-pb\dy; Bullet rebounds
		End If 
	
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
bem(i)\tp=1; (alive)

;set x and y starting coordinates but ensure they do not lie in the clear zone
Repeat 
bem(i)\x=Rnd(width)
bem(i)\y=Rnd(height)
Until bem(i)\x>xmax Or bem(i)\x<xmin Or bem(i)\y>ymax Or bem(i)\y<ymin

bem(i)\t=50
bem(i)\sp=3
Repeat
bem(i)\dx=Rnd(-1,1)
bem(i)\dy=Rnd(-1,1)
Until bem(i)\dx<>0 Or bem(i)\dy<>0
Next
End Function


Function update_bem()
bemcount=0
For i=0 To nbem
;If BEM alive do stuff
If bem(i)\tp<>0 Then 
	bemcount=bemcount+1

	DrawImage im_bem1,bem(i)\x,bem(i)\y
	;Text bem(i)\x,bem(i)\y,i

	;confine to screen
	bem(i)\dx=confine(bem(i)\x,bem(i)\dx,0,width)
	bem(i)\dy=confine(bem(i)\y,bem(i)\dy,0,height)

	;Update location
	bem(i)\x=bem(i)\x+bem(i)\dx
	bem(i)\y=bem(i)\y+bem(i)\dy

	;Caused bem to change direction when timer=0	
	bem(i)\t=bem(i)\t-1
	If bem(i)\t=0 Then
	bem(i)\t=30+Rnd(60)
	Repeat
	bem(i)\dx=Rnd(-1,1)
	bem(i)\dy=Rnd(-1,1)
	Until bem(i)\dx<>0 Or bem(i)\dy<>0
	End If 

	;Random chace of shooting at player
	If Rnd(1,1000)<bem(i)\sp Then
	pb.bullet=New bullet
	pb\x=bem(i)\x
	pb\y=bem(i)\y

	x=0.5*dist(player_x,player_y,bem(i)\x,bem(i)\y)
	pb\dx#=(player_x-bem(i)\x)/x
	pb\dy#=(player_y-bem(i)\y)/x

	pb\life#=width
	pb\fw=2
	End If

	;Collision detection between player bullet and BEM
	For pb.bullet = Each bullet
	If pb\fw=1 And ImagesCollide(im_bem1,bem(i)\x,bem(i)\y,0,im_bull1,pb\x,pb\y,0)
	bem(i)\tp=0;Changes status to dead
	bem(i)\sp=0;stops it shooting
	bem_explode(bem(i)\x,bem(i)\y); make explosion 
	bem(i)\dx=-100;moves it off the edge of the screen
	bem(i)\dy=-100
	pb\life#=0 ;removes the bullet
		End If 
	Next

	;Collision detection between pod and Bem
	For j=0 To npods
	If ImagesCollide(im_bem1,bem(i)\x,bem(i)\y,0,im_liz,pod(j)\x,pod(j)\y,0) Then 
		;Kill BEM if Pod is chaser
		If pod(j)\status=-1 Then
		bem_explode(bem(i)\x,bem(i)\y); make explosion
		bem(i)\tp=0;Changes BEM status to dead	
		bem(i)\sp=0;stops BEM from shooting
		pod(j)\status=1
		End If  

		;Rebound BEM if Pod is egg
		If pod(j)\status=1 Then
		bem(i)\dx=-bem(i)\dx
		bem(i)\dy=-bem(i)\dy
		End If 
	End If 
	Next; (j)

End If ;(From if bem\tp<>0. ie if not dead)

Next; (i)
End Function  

;#######################
;# Explosion Functions #
;#######################

Function bem_explode(x,y)
PlaySound sn_exp
bang(x,y,0,2,2,50,1)
bang(x,y,0,2,2,50,-4)
bang(x,y,0,2,2,50,-12)
End Function 

Function bang(x,y,o,dx,dy,max,count)
zob.zob=New zob
zob\count=count 
zob\max=max
zob\x1=x
zob\y1=y-o
zob\x2=x+o
zob\y2=y
zob\x3=x
zob\y3=y+o
zob\x4=x-o
zob\y4=y
zob\dx=dx
zob\dy=dy
End Function 


Function plotsqr() 
For zob.zob = Each zob
Color 0,256-zob\count*2,200
Line zob\x1,zob\y1,zob\x2,zob\y2
Line zob\x2,zob\y2,zob\x3,zob\y3
Line zob\x3,zob\y3,zob\x4,zob\y4
Line zob\x4,zob\y4,zob\x1,zob\y1
If zob\count>0
zob\y1=zob\y1-zob\dy
zob\x2=zob\x2+zob\dx
zob\y3=zob\y3+zob\dy
zob\x4=zob\x4-zob\dx
End If 
zob\count=zob\count+1
If zob\count>zob\max Then Delete zob
Next
End Function 



;###########################
;#General Perpose Fuctions #
;###########################

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


;Checks to see if any of the alive bems are closer than the player
For i=0 To nbem
j=dist(x,y,bem(i)\x,bem(i)\y)
If j<sep And bem(i)\tp<>0 Then
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