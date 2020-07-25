;Zap
PlayMusic("data\houseoffun.mid")

;go into graphics mode
Graphics 640,480
;set some scaling parameters
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;Sets some horizontal coordinates and stores in H(n)
steps#=20
Dim H(steps#)
For loop#=1.00 To steps#
H(loop#)=(loop#/steps#)*height
Next

;load fonts
Global fnt_mid=LoadFont("data\Silicon.ttf",16)
Global fnt_sm=LoadFont("data\Silicon.ttf",8)


;Load Graphics
;Tells Blitz Basic  to handle centring of images. (Important for rotation of images)
AutoMidHandle True 
Global im_clock=LoadImage("data\siniclock.bmp")
Global im_bull=LoadImage("data\bip.bmp")
Global im_fist=LoadImage("data\punch1.bmp")
Global im_logo=LoadImage("data\zaplogo.bmp")
Global im_pint=LoadImage("data\pint1.bmp")
Global im_pris=LoadImage("data\dsotm2.bmp")
Global im_ship=LoadImage("data\ship.bmp")
Global im_shot=LoadImage("data\shot.bmp")
Global im_sig=LoadImage("data\sig2.bmp")
Global im_snap=LoadAnimImage("data\teeth.bmp",45,33,0,2)
Global im_soup=LoadImage("data\soup.bmp")
Global im_swarm=LoadAnimImage("data\swarm.bmp",16,16,0,2)
 

;Sets background image
Global im_bak=setbak() 

;Image rotation settings
Global rots=32 ;number of orientations for spinning things
Dim trig#(rots,1)
Dim im_sigr(rots) ;stores images in different orientations
Dim im_prisr(rots);ditto

;Load Sounds
Global snd_exp=LoadSound("data\def_bip.wav")
Global snd_hit=LoadSound("data\bloop.wav")
Global snd_laser=LoadSound("data\def_laser.wav")
Global snd_oi=LoadSound("data\oi.wav")
Global snd_swarm=LoadSound("data\swarm.wav")
Global snd_level=LoadSound("data\zwoo.wav")


;Game Settings
Global level=0
Global levelname$=""
Global score=0
Global scroll_y
Global scroll_s=1
Global scroll_f=0;frame of scroll image
Global back_ht=ImageHeight(im_bak) 
Global loopcount=0;		Used to count the number of program loops
Global slowcount=loopcount; Used to store a fraction of loopcount for animation timing

;Create paths
Global max=720
Dim path(max,10)
For loop=0 To max
x=loop*width/max
y=loop*height/max
path(loop,0)=0							; null
path(loop,1)=x							; Straight x
path(loop,2)=width-x					; Straight -x
path(loop,3)=y							; Straight y
path(loop,4)=height-y					; Straight -y
path(loop,5)=40*Sin(90+loop*2)	        ; Small Sin
path(loop,6)=40*Cos(90+loop*2)      	; Small Cos
path(loop,7)=150*Sin(90+loop)      		; Med Sin
path(loop,8)=150*Cos(90+loop)      		; Med Cos
path(loop,9)=150*Sin(90+loop/2)      	; Big Sin
path(loop,10)=150*Cos(90+loop/2)      	; Big Cos
Next 

;player paramenters
Global player_x=midx
Global player_y=height-10
Global player_dx# ;x velocity
Global player_dy# ;y velocity
shipwidth=ImageWidth(im_ship)    ; width of ship image
shipheight=ImageHeight(im_ship)	 ; height of ship image
Global shipxmax=width-(shipwidth/2)	 ; maximum x-coord of ship
Global shipxmin=shipwidth/2
Global shipymax=height-(shipheight/2); maximum y-coord of ship
Global shipymin=shipheight/2


;************************
;*** Laser Parameters ***
;************************
;
Global nbeams=0    ;Number of beams in existence
Global maxbeams=4  ;Maximum number of symaltaneous beams allowed
Dim lascol(5,2)    ;Sort of colour palette for laser
Global lc=0         ;Colour index for laser beam (position of 1st coordinate in array)

;Stores primary and secondary rgb colour values in array 
Restore lcolourdata
For loop1=0 To 5
For loop2=0 To 2
Read num 
lascol(loop1,loop2)=num 
Next 
Next 

;Vertical laser beam
Type laser
Field x
Field y  ;Start point of beam
Field y2 ;end point of beam
Field ymax; maximum end point of beam
Field d   ; direction (1 = up the screen, -1= down) 
End Type 

;**********************
;*Explosion Parameters*
;**********************
Global expcol=0 ;explosion colour

Type expl
Field x
Field y
Field dx
Field dy
Field e ;lifetime of explosion
Field s	;Particle Size
Field c ;explosion colour
End Type

;*************************
;*** Baddie Parameters ***
;*************************

Global nbem=0; Number of baddies
Global scrolldown=height; Vertical displacement setting for all badies. Baddies start off the top of the screen and then scroll down to on-screen location at the start of each level.

;Data type for baddie
Type blob
Field x  ;x
Field y  ;y
Field xo ;x off-set
Field yo ;y off-set
Field xp ;x path type
Field yp ;y path type
Field dx ;x velocity
Field dy ;y velocity
Field o  ;orientation
Field p	 ;position in paths
Field dp ;movement rate through paths
Field im ;image type
Field mt ;movement type
Field hp ;hit points
End Type


;enable double buffering
SetBuffer BackBuffer()

rotate()
greet()
ch_music=PlayMusic("data\gddvibe.mid")

Repeat 
set_level(level)
setship(midx,400)
Cls
;loop until ESC hit...

Repeat
slowcount=loopcount/10

If (slowcount Mod(100))<50 
Cls
TileImage(im_bak),0,scroll_y,scroll_f
Else
TileImage(im_bak),0,scroll_y,scroll_f
TileImage(im_bak),0,scroll_y+1,scroll_f
End If 

scroll_y=(scroll_y+scroll_s) Mod(back_ht)     ;Scrolls the background image.
scroll_f=(slowcount Mod(18));Advances animation frame of background image.

update_blob(scrolldown)

;Beggining of level stuff. Scrolls the baddies onto the screen and displays level start message.
If scrolldown>0 Then 
scrolldown=scrolldown-2
Color 255,255,255
Text midx,scrolldown-midy,"Level "+level,True,True
Text midx,20+scrolldown-midy,levelname$,True,True 
End If  


control_ship()
update_ship()
update_laser
DrawImage(im_ship,player_x,player_y)
update_exp
Flip
loopcount=loopcount+1

Until KeyDown(1) Or nbem=0; (Until esc pressed on number of baddies (nbem)=0
loopcount=0
clearup()
level=level+1
scrolldown=height
Until KeyDown(1)
End 


;#######################################################################
;Functions Begin Here
;#######################################################################


;*********************************
;*** Game Management Functions ***
;*********************************

;Sets up each level
Function set_level(level)
SetFont fnt_mid
PlaySound(snd_level)
levelname$=""
loopcount=0

Select level 

Case 1
levelname$="Hmmmm ..."
d=Rnd(1,2)
For loop=0 To 11
z=loop*60
spawn(0,H(3),d,5,z,2,1)
spawn(0,H(5),d,5,z,1,1)
Next 

Case 2
levelname$="Cyclotron"
For loop=0 To 9
z=(loop*35)
spawn(midx-80,midy,7,8,z,1,1)
spawn(midx+80,midy,8,7,z,2,1)
Next

Case 3
levelname$="The Chattering Classes"
For loop=0 To 18
spawn(Rnd(0,width),0,5,3,Rnd(0,max),7,1)
Next

Case 4
levelname$="Four Soup Cycle"
For loop=0 To 3
z=loop*55
spawn(0,H(5),2,5,z,2,1)
spawn(0,H(5)-45,2,5,z,3,1)
spawn(0,H(10),1,5,z,2,1)
spawn(0,H(10)-45,1,5,z,3,1)
Next 

Case 5
levelname$="Did you spill my pint?"
For loop=0 To 5
z=(loop*40)
spawn(0,midy,1,0,z,4,1)
spawn(0,H(4),2,0,z,4,1)
Next 

Case 6
levelname$="Time Gentlemen Please"
spawn(0,midy,1,5,0,8,1)


Default
levelname$="Random House"
ra=Rnd(1,2)
y=Rnd(1,2)

If y=1 Then rb=0 Else rb=Rnd(3,10)

rc=Rnd(1,4)
rd=Rnd(5,11)
For loop=0 To 9
z=(loop*40)
spawn(0,H(rd),ra,rb,z,rc,1)
Next 

End Select
End Function

;Deletes all baddies
Function clearup()
For bem.blob = Each blob
Delete bem
Next
For pop.laser = Each laser
Delete pop
Next
nbeams=0 
End Function 

;************************
;*** Baddie Functions ***
;************************

;Creates baddie
Function spawn(xo,yo,xp,yp,p,im,mt)
bem.blob=New blob
bem\xo=xo
bem\xp=xp
bem\yo=yo
bem\yp=yp
bem\dx=0
bem\dy=0
bem\o=Rnd(rots) 
bem\p=p
bem\dp=4
bem\mt=mt
bem\im=im
bem\hp=1

If bem\im=6 Then bem\hp=4	;Sets hit points for fist image
If bem\im=8 Then bem\hp=16	;Sets hit points for Siniclock Image

If bem\mt=3 Then bem\dy=6	;Sets movement rate for objects that simply fall down the screen (fist)
If bem\mt=4 Then			;Sets movement for bullets 
delta_x=player_x-xo
delta_y=player_y-yo
If delta_y=0 Then delta_y=1
theta=ATan(delta_x/delta_y)
bem\dx=6*Sin(theta)
bem\dy=6*Cos(theta)
bem\hp=4
End If 


End Function 

;updates baddies
Function update_blob(scrolldown)


count=0; Resets baddie count

For bem.blob = Each blob
count=count+1
bem\x=bem\xo+path(bem\p,bem\xp)
bem\y=bem\yo+path(bem\p,bem\yp)-scrolldown

;Draws arrows pointing up to baddies if they are above the top of the screen.
If bem\y<0 Then
Color 255,0,0
Line bem\x,0,bem\x+3,3
Line bem\x,0,bem\x-3,3
End If 


; Homer - Updates mutant tomato things.
If bem\mt=2 Then
bem\xo=bem\xo+bem\dx
bem\yo=bem\yo+bem\dy
d=Rnd(1,5)
If bem\xo>player_x Then 
bem\dx=-d
Else bem\dx=d
End If 
If bem\yo>player_y Then 
bem\dy=-d
Else bem\dy=d
End If
End If


;Displays the various baddies.
If bem\im=1 Then DrawImage im_sigr(bem\o),bem\x,bem\y
If bem\im=2 Then DrawImage im_prisr(bem\o),bem\x,bem\y
If bem\im=3 Then DrawImage im_soup,bem\x,bem\y
If bem\im=4 Then DrawImage im_pint,bem\x,bem\y
If bem\im=5 Then DrawImage im_swarm,bem\x,bem\y,(slowcount Mod(2))
If bem\im=6 Then DrawImage im_fist,bem\x,bem\y
If bem\im=7 Then DrawImage im_snap,bem\x,bem\y,(slowcount Mod(2))
If bem\im=8 Then DrawImage im_clock,bem\x,bem\y
If bem\im=9 Then DrawImage im_bull,bem\x,bem\y


;advances position along path and wraps at ends
bem\p=bem\p+bem\dp 
If bem\p>max Then bem\p=0;max:bem\dp=-bem\dp
If bem\p<0 Then bem\p=0:bem\dp=-bem\dp

;Changes orientation. (only siginficant for rotated images)
bem\o=(bem\o+1) Mod(rots)

; plunger- Movement update for fists
If bem\mt=3 Then
bem\yo=bem\yo+bem\dy
If bem\y>height Then bem\hp=0 
End If 

;random chance of baddie shooting bullet
shoot=Rnd(500)
If (shoot=1 Or nbem<4) And level>0 Then 
spawn(bem\x,bem\y,0,0,0,9,4)
nbem=nbem+1
End If 

;Updates bullets
If bem\mt=4 Then 
bem\xo=bem\xo+bem\dx
bem\yo=bem\yo+bem\dy
If bem\x>width Or bem\x<0 Or bem\y>height Or bem\y<0 Then bem\hp=0
End If 

;Deletes  baddies that have no hps 
If bem\hp=0 Then 
Exp(bem\x,bem\y,20,50,1,expcol)	; Create Explosion
Delete bem						; Delete baddie
expcol=(expcol+1) Mod(6)		; Advance explosion colour index.
End If 

Next

nbem=count
Text 10,10,"Number of baddies: "+ nbem

End Function 

;generates rotated image
; To get centerimg right the statement"AutoMidHandle True" must appear in program before image loaded.
Function rotate()
For loop=0 To rots
angle=loop*360/rots
im_sigr(loop)=CopyImage(im_sig)
RotateImage im_sigr(loop),angle
im_prisr(loop)=CopyImage(im_pris)
RotateImage im_prisr(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
If KeyDown(1) End 
Next
End Function 


;************************
;*** Player Functions ***
;************************

;Set up new player ship initial conditions
Function setship(x,y)
player_x=x
player_y=y
player_dx#=0
player_dy#=0
End Function


Function update_ship()

;Stops ship from leaving edge of screen 
;player_dx#=confine(player_x,player_dx#,shipxmin,shipxmax)
;player_dy#=confine(player_y,player_dy#,shipymin,shipymax)


;Update ship position
player_x=player_x+player_dx#
player_y=player_y+player_dy#

If player_x<shipxmin Then player_x= shipxmin
If player_y<shipymin Then player_y= shipymin
If player_x>shipxmax Then player_x= shipxmax
If player_y>shipymax Then player_y= shipymax

End Function 


;Stops x# from exceeding min and max values by ensuring direction of velocity is towards the boundary if object leaves the edge.
;Used to stop things leaving the edge of the screen.
Function confine#(x#,dx#,min,max)
If x#<min And dx#<0 Then dx#=-dx#
If x#>max And dx#>0 Then dx#=-dx#
Return dx#
End Function


;Player controlls for ship
Function control_ship()
player_dx#=0:player_dy#=0

player_dx#=0
player_dy#=0

;If z or joystick pressed move ship left
If KeyDown(45) Or JoyXDir()=1 Then  player_dx#=8 

;If x or joystick pressed move ship right
If KeyDown(44) Or JoyXDir()=-1 Then player_dx#=-8

;If ; pressed then move ship up
If KeyDown(39) Or JoyYDir()=-1 Then player_dy#=-8

;If . pressed then move ship right
If KeyDown(52) Or JoyYDir()=1 Then player_dy#=8

;If fire button pressed fire laser if maximum number not already reached 
If (KeyHit(28) Or GetJoy()>0) And nbeams<maxbeams Then shoot_laser(player_x,player_y,480)
End Function 


;***********************
;*** Laser Functions ***
;***********************

;Initialises new beam
Function shoot_laser(x,y,l)
nbeams=nbeams+1
PlaySound(snd_laser)
pop.laser=New laser
pop\x=x
pop\y=y
pop\y2=y
pop\ymax=pop\y-l
If l>0 Then pop\d=1 Else pop\d=-1
End Function

;Updates beam.
Function update_laser()
For pop.laser = Each laser
Color lascol(lc,0),lascol(lc,1),lascol(lc,2)
Line pop\x,pop\y,pop\x,pop\y2
DrawImage im_shot,pop\x,pop\y2

;Adds speckles to beam
Color 0,0,0
For loop=0 To 10
y=Rnd(pop\y,pop\y2)
Line  pop\x,y,pop\x,y+4
Next

If pop\d=1 Then
dy1=6
dy2=1
dy3=12
Else
dy1=-6
dy2=-1
dy3=-12
End If 

;Lengthens beam if it has not reached full extension otherwise shortens it
If (pop\y2*pop\d)>=(pop\ymax*pop\d) Then 
pop\y2=pop\y2-dy1
pop\y=pop\y-dy2
Else pop\y=pop\y-dy3
End If 

For bem.blob = Each blob
;detects collision between upward moving laser and baddie and decreases hp if hit.
If pop\d=1 And ImageRectOverlap(im_sigr(bem\o),bem\x,bem\y,pop\x,pop\y2,1,1) Then 
bem\hp=bem\hp-1; decreases baddied hit points
pop\y=pop\ymax; will cause beam to die by trigering deletion later in this function

;Generates "refelected" beam if prism hit
If bem\im=2 Then
shoot_laser(bem\x,bem\y,-400)
End If

If bem\hp<>0  Exp(bem\x,bem\y,20,25,2,expcol)
PlaySound snd_hit
End If 


If bem\hp=0 Then 
PlaySound(snd_exp)
Exp(bem\x,bem\y,20,50,2,expcol)      ; create explosion
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
Exp(bem\x+6,bem\y-6,20,50,2,expcol)  ; create explosion
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
	
; spawns tomatoes If Soup Can destroyed!
If bem\im=3 Then 
spawn(bem\x+30,bem\y-40,5,6,0,5,2)
spawn(bem\x-30,bem\y-40,6,5,0,5,2)
spawn(bem\x+30,bem\y+40,5,0,0,5,2)
spawn(bem\x-30,bem\y+40,6,0,0,5,2)
PlaySound(snd_swarm) 
End If 

; spawns fist if pint destroyed!
If bem\im=4 Then 
spawn(bem\x,bem\y,0,0,0,6,3)
PlaySound(snd_oi) 
End If 

Delete bem ;delete baddie
End If 
Next 

;Deletes beam once length back to zero. Also shifts the beam colour.
If (pop\y*pop\d)<=(pop\ymax*pop\d) Then 
Delete pop
nbeams=nbeams-1; decreases beam count
lc=(lc+1)Mod 5 ; shifts the colour
End If 

Next
End Function

;rgb Colour Palette data for laser beam
.lcolourdata
Data 255,0,100
Data 255,255,0
Data 0,255,0
Data 0,255,255
Data 100,100,255
Data 255,0,255

;***************************
;*** Explosion Functions ***
;***************************
;
;Creates explosion centred on x,y made up of n particles 
;that will Last For l loops and are of size s and have colour index expcol.
Function Exp(x,y,n,l,s,expcol)
For loop=1 To n
pop.expl=New expl
pop\s=s
pop\e=l
pop\x=x
pop\y=y
pop\c=expcol
Repeat 
pop\dx=Rnd(-3,3)
pop\dy=Rnd(-3,3)
Until pop\dx<>0 Or pop\dy<>0
Next 
End Function 

;Updates explosion
Function update_exp() 
For pop.expl = Each expl

;Selects colour and brightness
c=4*pop\e
Select pop\c
Case 0 
Color c,0,0
Case 1
Color c,c,0
Case 2
Color 0,c,0
Case 3
Color 0,c,c
Case 4
Color c,0,c
Case 5
Color c,c,c
End Select 

;plots particles and expands to new locations
Rect pop\x,pop\y,pop\s,pop\s
;Rect pop\x-pop\dx,pop\y-pop\dy,pop\s,pop\s
Rect pop\x-(2*pop\dx),pop\y-(2*pop\dy),pop\s,pop\s

pop\x=pop\x+pop\dx
pop\y=pop\y+pop\dy
pop\e=pop\e-1
If pop\e<0 Then Delete pop 
Next
End Function 

;**********************
;*** Welcome Screen ***
;**********************

Function greet()
level=0
loop=0
bc=0
cc=0

For loop = 1 To 12
spawn(0,H(9),1,5,loop*20,5,1)
spawn(0,H(9),2,5,loop*20,5,1)
Next

Repeat
Cls
TileImage(im_bak),0,0,6
DrawImage im_logo,midx,H(3)
x=midx-150+path(loop,7):y=H(13)+(path(loop,8)*.6)
x1=width-x:y1=y
Exp(x,y,40,50,1,cc)
Exp(x1,y1,40,50,1,cc)
loop=(loop+2)Mod(max)
bc=(bc+1) Mod(55)
cc=bc/10.00

update_blob(0)
update_exp
SetFont fnt_mid
Text midx,h(18),"Press Space or Fire to Start",1,1
Color 255,255,255
Text midx-1,h(18)-1,"Press Space or Fire to Start",1,1
Flip

Until KeyDown(57) Or KeyDown(1) Or GetJoy()>0
clearup()
level=1
End Function 


;Creates multiframe background image
Function setbak()
SetFont fnt_sm
w=36
b=64
s=18
im_bak=CreateImage(w,b,s)
m=-4
For f=0 To (s-1)
SetBuffer ImageBuffer(im_bak,f)
If f<9 Then m=m+1 Else m=m-1
For y=-8 To b+8  Step 8
blue=Abs(y-32)+64
green=128-blue
;blue=100
Color 0,0,blue
For x=0 To w
If f<10 Then Plot x,(8+y+(m*Sin(x*10))) Else Plot x,(8+y+(m*Sin(x*10)))
Next 
Next 
;Color 130,0,80
;Text 16,16,f

Next 
Return im_bak
End Function 