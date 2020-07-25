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



;Load Graphics
;Tells Blitz Basic  to handle centring of images. (Important for rotation of images)
AutoMidHandle True 
Global im_sig=LoadImage("data\sig2.bmp")
Global im_pris=LoadImage("data\dsotm2.bmp")
Global im_ship=LoadImage("data\ship.bmp")

Global im_shot=LoadImage("data\shot.bmp")
Global im_logo=LoadImage("data\zaplogo.bmp")
Global im_pclock=LoadImage("data\horrible_clock_p.bmp")
Global im_swarm=LoadAnimImage("data\swarm.bmp",16,16,0,2)

Global im_bak=setbak() 



;Image rotation settings
Global rots=32 ;number of orientations for spinning things
Dim trig#(rots,1)
Dim im_sigr(rots) ;stores images in different orientations
Dim im_prisr(rots);ditto

;Load Sounds
Global snd_laser=LoadSound("data\def_laser.wav")
Global snd_exp=LoadSound("data\def_bip.wav")



;Game Settings
Global level=1 
Global score=0
Global scroll_y
Global scroll_s=1
Global scroll_f=0;frame of scroll image
Global back_ht=ImageHeight(im_bak) 

;Create paths
Global max=720
Dim path(max,7)
For loop=0 To max
y=loop*width/max
path(loop,0)=y
path(loop,1)=width-y
path(loop,2)=48*Sin(90+loop*2)+midy
path(loop,3)=48*Cos(90+loop*2)+midy
path(loop,4)=150*Sin(90+loop)+midy
path(loop,5)=150*Cos(90+loop)+midy
path(loop,6)=150*Sin(90+loop/2)+midy
path(loop,7)=150*Cos(90+loop/2)+midy
Next 

;player paramenters
Global player_x=midx
Global player_y=height-10
Global player_dx# ;x velocity
Global player_dy# ;y velocity
shipwidth=ImageWidth(im_ship)    ; width of ship image
shipheight=ImageHeight(im_ship)	 ; height of ship image
Global shipxmax=width-shipwidth	 ; maximum x-coord of ship
Global shipxmin=shipwidth/2
Global shipymax=height-shipheight; maximum y-coord of ship
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

Type laser
Field x
Field y  ;Start point of beam
Field y2 ;end point of beam
Field ymax; maximum end point of beam
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

;Data type for baddie
Type blob
Field x	 ;x coord
Field xo ;x off set (used for formations)
Field y	 ;y coord
Field yo ;y off set
Field dx ;x velocity
Field dy ;y velocity
Field o  ;orientation
Field p	 ;position in path
Field dp ;movement rate through path
Field im ;image type
Field mt ;movement type	
End Type


;enable double buffering
SetBuffer BackBuffer()

rotate()
greet()

Repeat 
set_level(level)
setship(midx,400)
Cls
;loop until ESC hit...
Repeat

Cls
TileImage(im_bak),0,scroll_y,scroll_f
scroll_y=(scroll_y+scroll_s) Mod(back_ht)
If scroll_y=1 Then scroll_f=(scroll_f+1)Mod(9)

update_blob()
control_ship()
update_ship()
update_laser
DrawImage(im_ship,player_x,player_y)
update_exp
Flip
Until KeyDown(1) Or nbem=0; (Until esc pressed on number of baddies (nbem)=0

clearup()
level=level+1
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
Select level 

Case 1
For loop=0 To 6
z=loop*60
spawn(path(0,0),path(0,1),0,-100,z,1,1)
spawn(path(0,0),path(0,1),0,-50,z,2,1)
spawn(path(0,0),path(0,1),0,-100,z,1,2)
spawn(path(0,0),path(0,1),0,-50,z,2,2)
Next 

Case 2
For loop=0 To 3
z=loop*60
spawn(path(0,0),path(0,0),z,0,1,1,2)
spawn(path(0,0),path(0,0),0,50,z,2,2)
Next 


Default
For loop=0 To 3
z=(loop*35)-90
spawn(path(0,0),path(0,1),0,z,1,1,3)
spawn(path(0,0),path(0,1),150,z,1,2,3)
spawn(path(0,0),path(0,1),300,z,1,1,3)
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
Function spawn(x,y,xo,yo,p,im,mt)
bem.blob=New blob
bem\x=x
bem\xo=xo
bem\y=y
bem\yo=yo
bem\dx=0
bem\dy=0
bem\o=Rnd(rots) 
bem\p=p
bem\dp=4
bem\mt=mt
bem\im=im
End Function 

;updates baddies
Function update_blob()
nbem=0
For bem.blob = Each blob
nbem=nbem+1
Select bem\mt 
Case 1
bem\x=bem\xo+path(bem\p,0)
bem\y=bem\yo+path(bem\p,3)

Case 2 
bem\x=bem\xo+path(bem\p,1)
bem\y=bem\yo+path(bem\p,3)

Case 3
bem\x=bem\xo+path(bem\p,4)
bem\y=bem\yo+path(bem\p,5)

Default
bem\x=bem\xo+path(bem\p,0)
bem\y=bem\yo+path(bem\p,0)


End Select 

If bem\im=1 Then DrawImage im_sigr(bem\o),bem\x,bem\y
If bem\im=2 Then DrawImage im_prisr(bem\o),bem\x,bem\y

bem\p=bem\p+bem\dp 
If bem\p>max Then bem\p=0;max:bem\dp=-bem\dp
If bem\p<0 Then bem\p=0:bem\dp=-bem\dp
bem\o=(bem\o+1) Mod(rots)
Next
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

Function shoot_laser(x,y,l)
nbeams=nbeams+1
PlaySound(snd_laser)
pop.laser=New laser
pop\x=x
pop\y=y
pop\y2=y
pop\ymax=pop\y-l
End Function

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

;Lengthens beam if it has not reached full extension otherwise shortens it
If pop\y2>=pop\ymax Then 
pop\y2=pop\y2-6
pop\y=pop\y-1
Else pop\y=pop\y-12
End If 

;detects collision between laser and baddie
For bem.blob = Each blob
If ImageRectOverlap(im_sigr(bem\o),bem\x,bem\y,pop\x,pop\y2,1,1) Then 
PlaySound(snd_exp)
Exp(bem\x,bem\y,20,50,2,expcol); create explosion
expcol=(expcol+1) Mod(6); Advances the explosion colour index
Exp(bem\x+6,bem\y-6,20,50,2,expcol); create explosion
expcol=(expcol+1) Mod(6); Advances the explosion colour index
Delete bem ;delete baddie
pop\y=pop\ymax; will cause beam to die by trigering deletion in the next part of this function
End If 
Next 


;Deletes beam once length back to zero. Also shifts the beam colour
If pop\y<=pop\ymax Then 
Delete pop
nbeams=nbeams-1
lc=(lc+1)Mod 5 
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
loop=0
bc=0
cc=0
Repeat
Cls
DrawImage im_logo,midx,H(3)
x=path(loop,4)-70:y=H(7)+(path(loop,5)*.6)
x1=width-x:y1=y
Exp(x,y,40,50,1,cc)
Exp(x1,y1,40,50,1,cc)
loop=(loop+2)Mod(max)
bc=(bc+1) Mod(55)
cc=bc/10.00
DrawImage im_pclock,midx,H(14) 
update_exp
DrawImage im_swarm,midx,H(15),(bc/8) Mod(2)
DrawImage im_swarm,midx,H(16),((bc/8)+1) Mod(2)
Flip 
Until KeyDown(28) Or KeyDown(1) Or GetJoy()>0
End Function 


;creates background image
Function setbak()
w=36
b=64
s=9
im_bak=CreateImage(w,b,s)

For f=0 To (s-1)
SetBuffer ImageBuffer(im_bak,f)
For y=0 To b  Step 1
blue=(y*1.5)+(f*6)
Color 0,0,blue
For x=0 To w
Plot  x,(8+y+(8*Cos(x*10)))
Next 
Next 
Next 
Return im_bak
End Function 




