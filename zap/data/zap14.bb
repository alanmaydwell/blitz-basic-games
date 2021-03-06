;Zap

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
Global fnt_sm=LoadFont("data\Silicon.ttf",12)


;Load Graphics
;Tells Blitz Basic  to handle centring of images. (Important for rotation of images)
AutoMidHandle True 
Global im_bull=LoadImage("data\bip.bmp")
Global im_cd=LoadImage("data\cd2.bmp")
Global im_clock=LoadImage("data\siniclock.bmp")
Global im_fist=LoadImage("data\punch1.bmp")
Global im_logo=LoadImage("data\zaplogo.bmp")
Global im_pint=LoadImage("data\pint1.bmp")
Global im_pris=LoadImage("data\dsotm2.bmp")
Global im_ship=LoadImage("data\ship.bmp")
Global im_shipsm=LoadImage("data\ship_small.bmp")
Global im_shot=LoadImage("data\shot.bmp")
Global im_sig=LoadImage("data\sig2.bmp")
Global im_snap1=LoadImage("data\teeth1.bmp")
Global im_snap2=LoadImage("data\teeth2.bmp")
Global im_soup=LoadImage("data\soup.bmp")
Global im_swarm1=LoadImage("data\swarm1.bmp")
Global im_swarm2=LoadImage("data\swarm2.bmp")
 

;Sets background image
Global im_bak=setbak() 

;Image rotation settings
Global rots=32 ;number of orientations for spinning things
Dim trig#(rots,1)
Dim im_sigr(rots) ;stores images in different orientations
Dim im_prisr(rots);ditto
Dim im_cdr(rots)  ;likewise

;Load Sounds
Global snd_exp=LoadSound("data\def_bip.wav")
Global snd_hit=LoadSound("data\bloop.wav")
Global snd_laser=LoadSound("data\def_laser.wav")
Global snd_oi=LoadSound("data\oi.wav")
Global snd_shipexp=LoadSound("data\explode.wav")
Global snd_swarm=LoadSound("data\swarm.wav")
Global snd_level=LoadSound("data\zwoo.wav")
SoundVolume snd_level,.7

;Game Settings
Global level=0;Current Level
Global startlevel=1; starting level
Global levelname$=""
Global score=0
Global scroll_y
Global scroll_s=1
Global scroll_f=0;frame of scroll image
Global back_ht=ImageHeight(im_bak) 
Global loopcount=0;		Used to count the number of program loops
Global slowcount=loopcount; Used to store a fraction of loopcount for animation timing

;High Score Table Settings
Global ns=4; 	Number of entries (-1) to be stored in the High Score Table. 
Dim hst(ns);	stores scores
Dim hsnames$(ns);Stores names


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
Global player_y=H(17)
Global player_dx# ;x velocity
Global player_dy# ;y velocity
shipwidth=ImageWidth(im_ship)    ; width of ship image
shipheight=ImageHeight(im_ship)	 ; height of ship image
Global shipxmax=width-(shipwidth/2)	 ; maximum x-coord of ship
Global shipxmin=shipwidth/2
Global shipymax=height-(shipheight/2); maximum y-coord of ship
Global shipymin=shipheight/2
Global status=2  ; Is ship status (2=awaiting resarection, 1=live, 0=dead)
Global life=4	 ; Current number of lives
Global nlife=4   ; Starting number of lives 
Global countin=1 ; Coundown to birth of ship


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
Global shootchance=500  ; Likelyhood of baddie shooting (Higher=less likely)

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


;*******************************************
;*** Intro Screen Scroll Text Parameters ***
;*******************************************

Type stext
Field A$ ;Text
Field y  ;Y coordinate
Field c	 ;Colour index
End Type 



;enable double buffering
SetBuffer BackBuffer()

sethighscore(ns); Fills highscore table with initial values.
rotate()        ; Generates rotated images.

;Outer loop
Repeat;
PlayMusic("data\houseoffun.mid")
greet()
life=nlife
ch_music=PlayMusic("data\gddvibe.mid")
countin=1
status=2
player_x=midx:player_y=H(17)


Repeat 
set_level(level)
;setship(midx,400)
Cls
;loop until ESC hit...
Repeat

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

; If ship is alive draw it and allow it to be controlled 
; else operate countdown to rebirth of ship.
If status=1;  
update_ship()
control_ship()
Else
countin=countin-1
If countin=0 Then status=1
End If 

update_laser
update_exp

SetFont fnt_sm
Text 21,6,life
DrawImage im_shipsm,10,10
Text 40,6,"Score: "+score


Flip
loopcount=loopcount+1

Until KeyDown(1) Or nbem=0 Or status=0; (Until esc pressed on number of baddies (nbem)=0

;Action taken if ship dead
If status=0 Then 
life=life-1
status=2; Set status to Awaiting Resarection.
countin=120; Set delay before ship materialises 
setship(midx,400)

;If life>0 Then clearup:scrolldown=height:set_level(level)
If life>-1 Then 
scrolldown=height*1.3
PlaySound(snd_level)
End If 

End If 

Until KeyDown(1) Or nbem=0 Or life <0
loopcount=0
clearup()
level=level+1
scrolldown=height
Until KeyDown(1) Or life<0
If KeyDown(1) End 
;Allows final explosions to grow after final ship destroyed.
For loop=0 To 100
update_exp
Flip 
Next
Delay 3500
update_highscores(score); Checks to see if highscore table should be updated.
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
levelname$="Gifts From AOL"
d=Rnd(1,2)
For loop=0 To 11
z=loop*60
;spawn(0,H(3),d,5,z,2,1)
;If loop Mod(2)=1 Then im=1 Else im=10
spawn(0,H(5),d,5,z,10,1)
Next 

Case 2
levelname$="SIG"
d=Rnd(1,2)
For loop=0 To 5
z=loop*44
;spawn(0,H(3),d,5,z,2,1)
;spawn(0,H(5),d,5,z,1,1)
spawn(-z,0,1,3,0,1,1)
spawn(z,0,2,3,0,1,1)
Next 


Case 3
levelname$="Are we there yet ?"
For loop=-3 To 3
spawn(midx+(loop*100),midy,7,5,0,1,1)
spawn(midx+(loop*100),H(7),7,5,180,10,1)
Next

Case 4
levelname$="Upon Reflection ..."
d=Rnd(1,2)
For loop=0 To 10
z=loop*60
spawn(0,H(3),d,5,z,2,1)
Next 

Case 5
levelname$="Cyclotron"
spawn(midx-80,H(8),0,0,0,1,1)
spawn(midx+80,H(8),0,0,0,1,1)
For loop=0 To 9
z=(loop*35)
spawn(midx-80,H(8),7,8,z,10,1)
spawn(midx+80,H(8),8,7,z,10,1)
Next

Case 6
levelname$="Did you spill my pint?"
For loop=0 To 5
z=(loop*40)
spawn(0,midy,1,0,z,4,1)
spawn(0,H(4),2,0,z,4,1)
Next 

Case 7
levelname$="The Chattering Classes"
For loop=0 To 18
spawn(Rnd(0,width),0,5,3,Rnd(0,max),7,1)
Next

Case 8
levelname$="Four Soup Cycle"
For loop=0 To 3
z=loop*55
spawn(0,H(5),2,5,z,2,1)
spawn(0,H(5)-45,2,5,z,3,1)
;spawn(0,H(10),1,5,z,2,1)
;spawn(0,H(10)-45,1,5,z,3,1)
Next 

Case 9
levelname$="Anti-Cyclotron"
For loop=0 To 9
spawn(midx-80,H(8),0,0,0,2,1)
spawn(midx+80,H(8),0,0,0,2,1)
Next
For loop=0 To 4
z=(loop*70)
spawn(midx-80,H(8),8,7,z,1,1)
spawn(midx+80,H(8),7,8,z,1,1)
Next


Case 10
levelname$="Time Gentlemen Please"
spawn(midx,midy,7,5,0,8,1)


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
For words.stext = Each stext
Delete words
Next 

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
bem\hp=4; (Hit points for bullets)
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


;Displays the various baddies.
im_temp=assign_img(bem\im,bem\o); Selects baddie image
DrawImage im_temp,bem\x,bem\y	; Draws image.

;Collision detection between baddie and ship if ship is alive (status 1)
If status=1 And ImagesCollide(im_temp,bem\x,bem\y,0,im_ship,player_x,player_y,0)
explode_ship() 

bem\hp=bem\hp-1
End If 


;advances position along path and wraps at ends
bem\p=bem\p+bem\dp 
If bem\p>max Then bem\p=0;max:bem\dp=-bem\dp
If bem\p<0 Then bem\p=0:bem\dp=-bem\dp

;Changes orientation. (only siginficant for rotated images)
bem\o=(bem\o+1) Mod(rots)

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

; plunger- Movement update for fists
If bem\mt=3 Then
bem\yo=bem\yo+bem\dy
If bem\y>height Then bem\hp=0 
End If 

;Causes siniclock to home in on player
If bem\im=8 Then 
bem\xo=bem\xo+bem\dx
bem\yo=bem\yo+bem\dy
If bem\xo>player_x Then 
bem\dx=-1
Else bem\dx=1
End If 
If bem\yo>player_y Then 
bem\dy=-1
Else bem\dy=1
End If 
End If 

;random chance of baddie shooting bullet
shoot=Rnd(shootchance)
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
If bem\hp<=0 Then 
Exp(bem\x,bem\y,20,50,1,expcol)	; Create Explosion
Delete bem						; Delete baddie
expcol=(expcol+1) Mod(6)		; Advance explosion colour index.
End If 

Next

nbem=count
;Text 10,10,"Number of baddies: "+ nbem

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
im_cdr(loop)=CopyImage(im_cd)
RotateImage im_cdr(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
If KeyDown(1) End 
Next
End Function 


;Selects appropriate image for each baddie
Function assign_img(n,o)

Select n
Case 1; Spectrun Logo (rotating)
im_temp=im_sigr(o)
Case 2; Prism (rotating)
im_temp=im_prisr(o)
Case 3; Soup
im_temp=im_soup
Case 4; Beer Glass
im_temp=im_pint
Case 5; Tomato
If slowcount Mod(2)= 1 Then 
im_temp=im_swarm1
Else im_temp=im_swarm2
End If 
Case 6; Fist
im_temp=im_fist
Case 7; Teeth (animated)
If slowcount Mod(2)= 1 Then 
im_temp=im_snap1
Else im_temp=im_snap2
End If 
Case 8; Clock
im_temp=im_clock
Case 9; Baddie Bullet
im_temp=im_bull
Case 10; cd
im_temp=im_cdr(o)
Default 
im_temp=im_bull
End Select 

Return im_temp
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

If player_x<shipxmin Then player_x= shipxmax
If player_y<shipymin Then player_y= shipymin
If player_x>shipxmax Then player_x= shipxmin
If player_y>shipymax Then player_y= shipymax

DrawImage(im_ship,player_x,player_y)

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

;Sets movement rates for updward (pop\d=1) and downward moving lasers
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


im_temp=assign_img(bem\im,bem\o); Selects baddie image type for collision detection.

;The actual collision detection step.
;If pop\d=1 And ImageRectOverlap(im_temp,bem\x,bem\y,pop\x,pop\y2,1,1) Then; With this line only the tip of the beam is deadly
If pop\d=1 And ImageRectOverlap(im_temp,bem\x,bem\y,pop\x,pop\y2,1,pop\y-pop\y2) Then ; With this line the whold beam is deadly


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
score=score+1						 ; Increases Score
	
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

;Detects collision between downward moving beam (d=-1) and ship.
If pop\d=-1 And ImageRectOverlap(im_ship,player_x,player_y,pop\x,pop\y,1,pop\y2-pop\y) Then
explode_ship
End If 

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

Function explode_ship()
If status=2 Return; Stops death and explosion taking place if ship is awaiting resurection.
status=0	;Set ship status to Dead.
Exp(player_x,player_y,20,100,3,expcol)
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
Exp(player_x+6,player_y+6,20,100,3,expcol)  
expcol=(expcol+1) Mod(6)
Exp(player_x-6,player_y-6,20,100,3,expcol)  
expcol=(expcol+1) Mod(6)
PlaySound(snd_shipexp)
End Function 

;**********************
;*** Welcome Screen ***
;**********************

Function greet()
player_x=midx:player_y=midy
level=0
loop=0
score=0

cc=0	; Explosion colour index.
bc=0	; 10 x the explosion colour index.

set_text

;Sets up a siniclock
spawn(midx,height+100,7,5,0,8,1)


;Sets up some sinusoidal tomatoes
For loop = 1 To 12
spawn(0,H(16),1,5,loop*20,5,1)
spawn(0,H(16),2,5,loop*20,5,1)
Next

Repeat
Cls
TileImage(im_bak),0,0,6
update_text(bc)
DrawImage im_logo,midx,H(3)

;Draws swirly explosion things 
x=midx-150+path(loop,7):y=H(13)+(path(loop,8)*.6)
x1=width-x:y1=y
Exp(x,y,40,50,1,cc)
Exp(x1,y1,40,50,1,cc)
loop=(loop+2)Mod(max)
bc=(bc+1) Mod(55)
cc=bc/10.00

slowcount=cc; Slowcount controlls animation for tomatoes 
update_blob(0)
update_exp
 

SetFont fnt_mid
Text midx,h(18),"Press Space or Fire to Start",1,1
Color 255,255,255
Text midx-1,h(18)-1,"Press Space or Fire to Start",1,1

If KeyDown(24) options 
Flip
Until KeyDown(57) Or KeyDown(1) Or GetJoy()>0
clearup()
level=startlevel
End Function

;Sets Scrolling Text For Intro Screen
Function set_text()
temp_c=0
words.stext=New stext:words\y=H(0):words\A$="CONTROLLS"
words.stext=New stext:words\y=H(1):words\A$="Z - Left"
words.stext=New stext:words\y=H(2):words\A$="X - Right"
words.stext=New stext:words\y=H(3):words\A$="; - Up"
words.stext=New stext:words\y=H(4):words\A$=". - Down"
words.stext=New stext:words\y=H(5):words\A$="Return - Fire"
words.stext=New stext:words\y=H(6):words\A$="Or use a joystick"
words.stext=New stext:words\y=H(7):words\A$="O - Options Screen"
words.stext=New stext:words\y=H(8):words\A$="Esc - Quit"

;Adds highscore display
words.stext=New stext:words\y=H(13):words\A$="HIGH SCORES"
For loop=0 To ns
words.stext=New stext:words\y=H(14+loop):words\A$=hst(loop)+"   "+hsnames$(loop)
Next


;Assigns colour value to each line of text
For words.stext = Each stext
words\c=temp_c
temp_c=(temp_c+1)Mod 5
Next 


End Function 

;Updates Scrolling Text For Intro Screen
Function update_text(bc)
SetFont fnt_sm 
If bc Mod(6)=1 Then colchange=True Else colchange=False 

For words.stext = Each stext
Color lascol(words\c,0),lascol(words\c,1),lascol(words\c,2)
If colchange=True Then words\c=(words\c+1) Mod(5) 
Text midx,words\y,words\A$,1,1
words\y=words\y-2
If words\y<=-400 Then words\y=height 
Next

End Function 

;Options screen
Function options()
sel=1	; Current position in list
selmin=1; Min position in list
selmax=5; Max position in list
quit=False
Repeat
Cls
Color 0,255,255
Text midx,H(3),"Use cursor keys to select and change values",True,True 
;Moves position in list up or down if up or down arrow pressed
If (KeyHit(200) Or JoyYDir()=-1) And sel>selmin Then sel=sel-1
If (KeyHit(208) Or JoyYDir()=1) And sel<selmax Then sel=sel+1

;Highlights currently selected item and allows 
;associated parameter to be adjusted by calling
;Function change_val
Color 200,0,255
Select sel
Case 1
Rect 0,H(7),width,18
nlife=change_val(nlife,0,20,1) 
Case 2
Rect 0,H(9),width,18
maxbeams=change_val(maxbeams,1,30,1) 
Case 3
Rect 0,H(11),width,18
shootchance=change_val(shootchance,0,2000,100) 
Case 4
Rect 0,H(13),width,18
startlevel=change_val(startlevel,1,10,1) 
Case 5
Rect 0,H(15),width,18
If KeyDown(203) Or KeyDown(205) Or GetJoy()>0 Then quit=True 
End Select 

;Decoration
TileImage im_bak,x,0,3+sel
x=x+1 Mod(32)
f=(x Mod(18))

Color 255,255,255
Text midx,H(1),"O P T I O N S",1,0

;Display text 
Color 255,255,0
Text midx,H(7),"Spare Lives: "+nlife,1,0
Text midx,H(9),"Maximum Player Shots: "+maxbeams,1,0
Text midx,H(11),"Baddie Shot Trigger: "+shootchance,1,0
Text midx,H(13),"Starting Level: "+startlevel,1,0
Color 255,100,100
Text midx,H(15),"EXIT",1,0
Flip
Until KeyDown(1) Or quit=True
FlushKeys 
End Function  

;Increases or decreases value when right or left arrow key pressed
;and if value does not exceed max or min.
;(Used by options screen)
Function change_val(temp,min,max,delta)
If KeyHit(203) Or JoyXDir()=-1  And temp>min Then temp=temp-delta
If KeyHit(205) Or JoyXDir()=1 And temp<max Then temp=temp+delta
Return temp 
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

;***************************
;*** Highscore Functions ***
;***************************
;
;Fills table with initial blank values
Function sethighscore(ns)
For loop=0 To ns
hst(loop)=0
hsnames$(loop)="Ho Ho Ho!"
Next
End Function

;Displays High Score Table
Function display_highscores(ns)
Cls 
Color 255,255,255
For loop=0 To ns
Print hst(loop)+"   "+hsnames$(loop)
Next
Flip 
WaitKey 
End Function

;Places new score in High Score Table if value is high enough
Function update_highscores(score)

;Checks high score table to see if the new score should be included.
;If new score is to be included its position in table is stored in np.
;-1 value of np is used to signify that score should not be added because it is too low.

np=-1 
count=0
Repeat
If score>=hst(count) Then np=count
count=count+1 
Until np<>-1 Or count>ns


;If new score to be inclued, existing entries from bottom of table to np are moved down one place.
;Except when new score is to be added to the final position (not necessary in such a case).
If np<>-1 And  np<>ns Then 
For loop=ns To np+1 Step -1
hst(loop)=hst(loop-1)
hsnames$(loop)=hsnames$(loop-1)
Next
End If 

If np<>-1 Then 
;adds new score to high score table at position np. 
hst(np)=score
;requests player's name name and stores it in table.
hsnames$(np)=inputname$(np)
End If
End Function


Function inputname$(np)
a$=""
Color 255,255,255
SetFont fnt_mid
Repeat 
Cls 

;Generates Random Explosions for background
x=Rnd(0,width):y=Rnd(0,height):s=Rnd(1,6)
Exp(x,y,20,50,s,expcol)      ; create explosion
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
Exp(x+6,y-6,20,50,s,expcol)  ; create explosion
expcol=(expcol+1) Mod(6)             ; Advances the explosion colour index
update_exp
Color 255,0,100
Text midx,H(2),"Congratulations!",1,1
Color 255,200,0
Text midx,H(3),"Your score of "+score,1,1
Color 220,220,0
Text midx,H(4),"has placed you at position "+(np+1),1,1
Color 0,255,100
Text midx,H(5),"in the High Score Table",1,1
Color 0,140,255
Text midx,H(8),"Please type your name:",1,1

; Checks for keypress and adds corresponding key to the string 
; if it is not blank or backspace or return and string has not 
; reached maximum value..
temp=GetKey()
If temp>0 And temp<>13 And temp<>8 And Len(a$)<17 Then a$=a$+Chr$(temp)

;Deletes character from string if Backspace pressed.
If temp=8 And Len(a$)>0 Then 
l=Len(a$)-1
a$=Left$(a$,l)End If

Color 255,255,255 
Text midx,H(10),a$,1,1 

Flip
If KeyDown(1) End;quit if escape pressed
Until temp=13; Ends when return key pressed

If a$="" Then a$="anon"
Return a$
End Function 