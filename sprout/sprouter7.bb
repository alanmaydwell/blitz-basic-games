;The Sprouter Limits
;An asteroids type game.
;By Alan Maydwell
;Feb 2002


;go into graphics mode
Graphics 640,480
;Stores width and height of screen for scaling purposes
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global szx=(width/2)-100; x Coordinate of corner "safe zone" for ship to materialise.
Global szy=(height/2)-100; y
Global hectic=False; Hectic mode flag. If "true" sprouts are attracted to ship.
Global hectic_trig; sets number of sprouts below which hectic mode is triggered
Global bpcount=0;records number of bullets launched at one time
Global range=500;missile range
Global mcolour#=255.000/range; scale factor linking missile colour to its range
Global maxbpcount=4;Maximum number of simultaneous bullets
Global startlives=3;Number of starting lives per game
Global startenergy=30;Starting ship shield energy
Global pause=0;game slow-down factor
Global score=0 ;stores score
Global rots=36 ;number of orientations for ship and sprouts

Dim playership(rots) ;stores ship images in different orientations
Dim b_sproutoid(rots); stores big sproutoid images in different orientations
Dim s_sproutoid(rots); stores small  sproutoid images in different orientations
Dim trig#(rots,1) ;Stores Sin and Cos look-up table.

;enable double buffering
SetBuffer BackBuffer()

;Tells Blitz Basic  to handle centring of images
AutoMidHandle True  

;Load Graphics
shipimg=LoadImage("data\ship.bmp")
b_sproutimg=LoadImage("data\midsprout.bmp")
s_sproutimg=LoadImage("data\minisprout.bmp")
Global backgrnd=LoadImage("data\bluegrad.bmp")

;Load Fonts
fnt_big=LoadFont("data\blaster.ttf",36,False,False,False)
fnt_mid=LoadFont("data\blaster.ttf",24,False,False,False)
fnt_txt=LoadFont("data\blaster.ttf",14,False,False,False)
SetFont fnt_txt

;Load Sounds
snd_shoot=LoadSound("data\shoot.wav")
snd_exp=LoadSound("data\explosion.wav")
snd_shipexp=LoadSound("data\crash.wav")
snd_bump=LoadSound("data\ow.wav")
snd_burp=LoadSound("data\burp.wav")
snd_food=LoadSound("data\food_fight.wav")
snd_hooray=LoadSound("data\allel.wav")
snd_laugh=LoadSound("data\elaugh.wav")
snd_engine=LoadSound("data\thrust.wav")
snd_rico=LoadSound("data\rico.wav")
snd_jaws=LoadSound("data\jaws.wav")

;sets up engine sound
LoopSound snd_engine:chnEngine=PlaySound(snd_engine):PauseChannel chnEngine:SoundVolume snd_engine,0.93

;Sets up hectic mode sound
LoopSound snd_jaws:chnJaws=PlaySound(snd_jaws):PauseChannel chnJaws
story(fnt_big,fnt_mid,fnt_txt); Displays blurb on screen.

;Rotates the ship image and stores separate playership in playership(loop)
;Does similar job for the sprouts!
;Also creates sin and cos look up table
;Takes a few seconds
For loop=0 To rots-1
angle=loop*360/rots
playership(loop)=CopyImage(shipimg)
b_sproutoid(loop)=CopyImage(b_sproutimg)
s_sproutoid(loop)=CopyImage(s_sproutimg)
RotateImage playership(loop),angle
RotateImage b_sproutoid(loop),angle
RotateImage s_sproutoid(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
;Print loop+" / " + (rots-1) +" Angle, " +angle +" Sin, " +trig#(loop,0) +" Cos, " +trig#(loop,1)
Next

;Waits until space is pressed.
Color 255,0,100
Print"                        Press Space to continue"
Repeat
Until KeyDown(57)
Delay 300 

;Type ship parameters
Global player_x	;x coord
Global player_y	;y coord
Global player_dx# ;x velocity
Global player_dy# ;y velocity
Global player_o	;orientation
Global player_e ;energy
Global cs#; Conversion factor to relate sheild colour to energy.

;bullet type
Type bullet
Field x,y
Field dx#,dy#
Field life#
Field fromwhom
End Type

;Sprouttype
Type blob
Field x	;x coord
Field y	;y coord
Field dx# ;x velocity
Field dy# ;y velocity
Field o  ;orientation
Field s ;size
Field ddx#; x acceleration
Field ddy#; y acceleration
End Type

While Not KeyDown(1); Outer loop
SetFont fnt_mid
chMusic=PlayMusic("data\toobin.mod"); Loads and plays background music
greet(fnt_big,fnt_mid,fnt_txt);welcome screen
StopChannel (chMusic); stops music playing  
score=0
level=1
PlaySound snd_food


;Set up player ship
lives=startlives
setship(width/2,height/2)


;Main loop. Keeps going until escape pressed
Repeat; Game in progress loop
nsprout=5+(level*2);number of sprouts per level
hectic_trig=3+level

;Generate Sproutoids (x,y,size)
For loop=1 To nsprout
Repeat:x=Rnd(50,width-50):Until (x<szx Or x>(szx+200))
Repeat:y=Rnd(50,height-50):Until (y<szy Or y>(szy+200))
newsprout(x,y,1)
Next

Repeat; Level in progress loop
Cls

;Draws border to screen with colour that varies with ship coordinates
Color 255,player_x,player_y
Rect 0,0,width,height,0
Rect 2,2,(width-4),(height-4),0

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
ResumeChannel chnEngine; makes engine sound
player_dx#=player_dx#+(.1*trig#(player_o,0)) 
player_dy#=player_dy#-(.1*trig#(player_o,1))
Else PauseChannel chnEngine
End If

;friction. Automatically slows the ship
player_dx#=player_dx#*.999
player_dy#=player_dy#*.999


;Update ship position
player_x=player_x+player_dx#
player_y=player_y+player_dy#

;Stops ship from leaving edge of screen
If player_x<0 Then player_x=0: player_dx#=-player_dx#:PlaySound snd_bump
If player_x>width Then player_x=width: player_dx#=-player_dx#:PlaySound snd_bump
If player_y<0 Then player_y=0: player_dy#=-player_dy#:PlaySound snd_bump
If player_y>height Then player_y=height: player_dy#=-player_dy#:PlaySound snd_bump


;If fire button pressed lauch missile and set missile speeds
If KeyHit(28) And bpcount<maxbpcount Then
bpcount=bpcount+1 
pb.bullet=New bullet
pb\x=player_x
pb\y=player_y
pb\dx#=player_dx#+8*trig#(player_o,0)
pb\dy#=player_dy#-8*trig#(player_o,1)
pb\life#=range
PlaySound snd_shoot
End If


;Update missiles if launched
If bpcount<>0 Then
For pb.bullet = Each bullet
Color 255,pb\life#*mcolour#,55
Oval pb\x,pb\y,4,4
pb\x=pb\x+pb\dx#
pb\y=pb\y+pb\dy#
pb\life#=pb\life#-(Sqr(pb\dx#^2+pb\dy#^2))


;Detects collision between bullet and blob
For sprout.blob = Each blob

;for big sprouts
If sprout\s=1 And ImageRectOverlap(b_sproutoid(sprout\o),sprout\x,sprout\y,pb\x,pb\y,4,4)
Oval sprout\x-50,sprout\y-50,100,100
sprout\s=0 					;Shrinks sprout
sprout\dx#=sprout\dx#*2		; speeds it up x
sprout\dy#=sprout\dy#*2		;  "      "  " y
newsprout(sprout\x,sprout\y,0); generates new sprout
sprout\dx#=-sprout\dx#
sprout\dy#=-sprout\dy#
newsprout(sprout\x,sprout\y,0)
pb\life#=0
PlaySound snd_burp
score=score+1
End If 

; for small sprouts
If sprout\s=0 And ImageRectOverlap(s_sproutoid(sprout\o),sprout\x,sprout\y,pb\x,pb\y,4,4)
Oval sprout\x-25,sprout\y-25,50,50
Delete sprout
pb\life#=0
PlaySound snd_exp
score=score+2
End If 
Next

;Confines bullets to screen
If pb\x<0 Then pb\x=0:pb\dx#=-pb\dx#:PlaySound snd_rico
If pb\y<0 Then pb\y=0:pb\dy#=-pb\dy#:PlaySound snd_rico
If pb\x>width Then pb\x=width:pb\dx#=-pb\dx#:PlaySound snd_rico
If pb\y>height Then pb\y=height:pb\dy#=-pb\dy#:PlaySound snd_rico

; kill missile at edge of range
If pb\life#<=0 Then Delete pb:bpcount=bpcount-1
Next
End If 


;Draw sproutoids
DrawImage playership(player_o),player_x,player_y

sproutcount=0
For sprout.blob = Each blob
Gosub drawsprouts
If hectic=True Gosub attraction; Causes sprouts to accelerate towards ship if less than 5 sprouts left
sproutcount=sproutcount+1


;Detects collision between sproutoid and ship
If sprout\s=1 And ImagesCollide(playership(player_o),player_x,player_y,0,b_sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_bump)
i=player_e*cs#
Color 0,i,255
Oval player_x-18,player_y-18,36,36,0
Oval player_x-20,player_y-20,40,40,0
sprout\dx#=-sprout\dx#
sprout\dy#=-sprout\dy#
player_dx#=-player_dx#
player_dy#=-player_dy#
player_e=player_e-1
End If


If sprout\s=0 And ImagesCollide(playership(player_o),player_x,player_y,0,s_sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_bump)
i=player_e*cs#
Color 0,i,255
Oval player_x-18,player_y-18,36,36,0
Oval player_x-20,player_y-20,40,40,0
sprout\dx#=-sprout\dx#
sprout\dy#=-sprout\dy#
player_dx#=-player_dx#
player_dy#=-player_dy#
player_e=player_e-1
End If

;Explode the ship if energy 0
If player_e<=0 Then
PauseChannel chnEngine ; Stop engine noise
PlaySound snd_shipexp  ; Make explosion noise

;draw circular explosion centred on ship

For loop=1 To 800 Step 8
shift=loop/2
shift2=loop/4
Color 255,255-shift,shift
Oval player_x-shift,player_y-shift,loop,loop,0
Color 0,0,0
Oval player_x-shift2,player_y-shift2,shift,shift,1
Flip
Next
lives=lives-1
player_e=20;Bodge to stop the explosion repeating for each remaining sprout after final life used. (If not final life player_e will be reset by line below).
If lives >0 Then Gosub restoreship:Exit ;Resets ship if lives remaining. Wait till coast is centre of screen is clear.
End If 
Gosub updatesprouts
Next

If sproutcount<hectic_trig Then; Sets hectic mode if number of sprouts less than trigger value
hectic=True
ResumeChannel chnJaws
ClsColor 0,green,0
green=green+dg
If green>180 Then dg=-4
If green=0 Then dg=4
End If 

SetFont fnt_txt
Text 10,10,"LEVEL:"+level +"  ENERGY:"+player_e +"  LIVES:"+ lives+"  SCORE:"+score
Flip
Delay pause; Slowdown factor if game runs too fast
Until sproutcount=0 Or lives<=0 Or KeyDown(1)  

;level has ended

;Turns off hectic mode and stops hectic mode sound effect
hectic=False
PauseChannel chnJaws  


;desplays brief end of level message if lives>0 and increases ship energy
If lives>0
PlaySound snd_hooray
ClsColor 0,0,200
Cls
SetFont fnt_mid
Text width/2,height*.4,"Level "+level +" Complete",True,True
Text width/2,height*.5,"Energy Bonus: "+ level*2,True,True
player_e=player_e+(level*2)
Flip 
Delay 1500
End If 

ClsColor 0,0,0 
level=level+1
clear();deletes sproutoids and bullets

Until lives<=0 Or KeyDown(1)

;game now over. All lives gone.
Delay 1000
PlaySound snd_laugh
Cls 
Color 255,255,255
SetFont fnt_mid
Text width/2,height/2,"GAME OVER",True,True
Text width/2,height*.6,"Score: "+score,True,True 
Flip 
Delay 2500 

Wend  
End 




;Deletes all sprouts and missiles
Function clear()
;deletes sprouts
For sprout.blob = Each blob
Delete sprout
Next
;deletes missiles
For pb.bullet = Each bullet
Delete pb
Next
bpcount=0
End Function 

;Generates new sproutoids
Function newsprout(x,y,s)
sprout.blob=New blob 
sprout\x=x
sprout\y=y
Repeat:sprout\dx#=Rnd(-2.0,2.0):Until Abs(sprout\dx#)>0.5
Repeat:sprout\dy#=Rnd(-2.0,2.0):Until Abs(sprout\dy#)>0.5
sprout\o=(Rnd(1,rots))-1
sprout\s=s
End Function 

;Set up new player ship initial conditions
Function setship(x,y)
player_x=x
player_y=y
player_dx#=0
player_dy#=0
player_o=0
player_e=startenergy
cs#=255.00/player_e
End Function

;Checks coast is clear before starting new life in centre of screen
.restoreship
Delay 1000
tx=width/2
ty=height/2
c=100
timer=1000
Repeat
sproutcount=0
safe=True
c=c+1:If c>255 Then c=100
Cls
Color c,c,0
Text tx,ty*.5,"AWAITING CLEAR MATERIALISATION ZONE "+ timer,True,True  

;Updates the sprouts and checks to see whether they are in central zone. If timer reaches 0 then sprouts are forced towards the bottom of the screen
For sprout.blob = Each blob
sproutcount=sproutcount+1
Rect szx,szy,200,200,0
If sprout\s=1 And ImageRectOverlap(b_sproutoid(sprout\o),sprout\x,sprout\y,szx,szy,200,200) Then safe=False
If sprout\s=0 And ImageRectOverlap(s_sproutoid(sprout\o),sprout\x,sprout\y,szx,szy,200,200) Then safe=False

;If timer is 0 then sprouts are forced towards the bottom of the screen but are prevented from moving too fast.
If timer=0 Then
Color 0,c,c 
Text tx,ty,"TRACTOR BEAM ON",True,True 
sprout\dy=sprout\dy+1
sprout\dy#=sprout\dy#+.001
If Abs(sprout\dy#)>4 Then sprout\dy#=sprout\dy#*.8
End If

Gosub updatesprouts
Gosub drawsprouts
Next

timer=timer-1
If timer<1 Then timer=0
Flip
Until safe=True
setship(tx,ty)
Return


;Updates the Sprouts
.updatesprouts
sprout\o=sprout\o+1
If sprout\o=rots Then sprout\o=0
sprout\x=sprout\x+sprout\dx#
sprout\y=sprout\y+sprout\dy#
If sprout\x<0 Then sprout\x=0: sprout\dx#=-sprout\dx#
If sprout\x>width Then sprout\x=width: sprout\dx#=-sprout\dx#
If sprout\y<0 Then sprout\y=0: sprout\dy#=-sprout\dy#
If sprout\y>height Then sprout\y=height: sprout\dy#=-sprout\dy#
sprout\dx#=sprout\dx#+sprout\ddx#
sprout\dy#=sprout\dy#+sprout\ddy#
Return

.attraction; Causes sprouts to be accelerate towards the ship but also limits maximum speed
If sprout\x>player_x Then sprout\ddx#=-0.40 Else sprout\ddx=.04
If sprout\y>player_y Then sprout\ddy#=-0.40 Else sprout\ddy=.04
If Abs(sprout\dy#)>5 Then sprout\dy#=sprout\dy#*.80
If Abs(sprout\dx#)>5 Then sprout\dx#=sprout\dx#*.80

Return 

;draws the sprouts 
.drawsprouts
If sprout\s=1 Then DrawImage(b_sproutoid(sprout\o),sprout\x,sprout\y)
If sprout\s=0 Then DrawImage(s_sproutoid(sprout\o),sprout\x,sprout\y)
Return


;-----------------------------------------------
;Welcome Screen
Function greet(fnt_big,fnt_mid,fnt_txt)
y1=0
y2=0

SetBuffer BackBuffer()
ymax1=ImageHeight(b_sproutoid(1))
ymax2=ImageHeight(s_sproutoid(1))

;Y coordinates for text. Scaled to screen.
top=height*.1
h0=height*.15
h1=height*.3
h2=height*.35
h3=height*.4
h4=height*.45
h5=height*.55
h6=height*.7
h7=height*.9
h8=height*.97
;x coordinates for centre of each text line
x=width/2


Repeat 
Cls 
TileBlock s_sproutoid(1),0,y2
TileImage b_sproutoid(1),0,y1
i=4*y1;colour intensity

Color 255-i,255-i,255
SetFont fnt_big
Text x,top,"The Sprouter Limits",True,True
SetFont fnt_txt
Color 255-i,255-i,0
Text x,h0,"A sort of asteroids game by Alan Maydwell",True,True 
SetFont fnt_mid
Color i,255,i 
Text x,h1,"X - Right",True,True 
Text x,h2,"Z - Left",True,True 
Text x,h3,"Right-Shift - Thrust",True,True 
Text x,h4,"Enter - Fire",True,True
Color 255-i,255,255-i
Text x,h5,"press O for options",True,True
Color i,i,i
Text x,h6,"Last Score: "+score,True,True 
Color 255,0,0
Rect width/4,h7-12,315,24 
Color 255,i,i
Text x,h7,"Press space bar to start",True,True
Color 0,255,255
SetFont fnt_txt
Text x,h8,"Hold escape key for a few seconds to quit from game",True,True 
y1=y1+2:If y1>ymax1 Then y1=0 
y2=y2+1:If y2>ymax2 Then y2=0 
Flip
If KeyDown(24) option(fnt_big,fnt_mid,fnt_txt); goto options screen if O is pressed
Until KeyDown(57) Or KeyDown(1)
End Function

;Options Screen
Function option(fnt_big,fnt_mid,fnt_txt)
ClsColor 0,0,100
FlushKeys ;clears keyboard buffer
;heights for drawing text scaled to screen
h1=height*.05
h2=height*.2
h3=height*.3
h4=height*.4
h5=height*.5
h6=height*.6
h9=height*.9

midx=width/2; x coord halfway across screen

Repeat 
Cls
TileImage backgrnd
SetFont fnt_big
Color 255,255,255
Text midx,h1,"Options",True 
Color 0,255,255
Text 0,h2,"(F1) lives "+startlives
Text 0,h3,"(F2) shield energy "+startenergy 
Text 0,h4,"(F3) maximum missiles "+maxbpcount
Text 0,h5,"(F4) missile range "+range
Text 0,h6,"(F5) game speed reduction (ms) "+pause
Color 200,100,255
Text midx,h9,"Press Space To Continue",True,True 
;Repeat
If KeyDown(59) startlives=getval%("lives ",startlives,1)
If KeyDown(60) startenergy=getval%("energy ",startenergy,1)
If KeyDown(61) maxbpcount=getval%("missiles ",maxbpcount,1)
If KeyDown(62) range=getval%("range ",range,1)
If KeyDown(63) pause=getval%("delay ",pause,0)
Flip
Until KeyDown(57) Or KeyDown(1)
mcolour#=255.000/range; scale factor linking missile colour to range.
If pause>1000 Then pause=1000
ClsColor 0,0,0 
Delay 300
End Function 

;Takes integer input and ensures that value is at least minimum
Function getval%(A$,value,min)
Color 100,130,255
Locate width*.3,height*.7
value=Input(A$)
If value<min Then value =1 
Return value
End Function 


Function story(fnt_big,fnt_mid,fnt_txt)
Cls
SetFont fnt_mid
Color 100,255,0
Print"THE SPROUTS ARE REVOLTING!"
SetFont fnt_txt
Print""
Print""
Print"No one knows where they came from. It may have been a GM crop experiment gone"
Print"horribly wrong, it may have been a result of contanimation from nuclear testing,"
Print"or maybe, they had been biding their time for millenia, hatching their evil" 
Print"plans and awaiting their moment to strike."
Print""
Print"Whatever the cause, giant mutant sprouts started appering all over the earth,"
Print"marauding throughout the world casusing death and destruction wherever they"
Print"went."
Print""
Print"Finaly the Veg Control Authority with the aid of specially trained rabbits "
Print"have contained the ring-leaders within a vessel of pure bolonium, the only"
Print"known material strong enough to contain them."
Print""
Print"It was your task to enter the containment vessel and do battle with the evil"
Print"sproutoids until they were uterly destroyed. To your relief you quickly"
Print"managed to wipe them out. However, to your horror, no sooner had the last" 
Print"sproutoid been destroyed when a New wave sproutoids materialised through a" 
Print"demesional wormhole."
Print""
Print"A message appears on your communications screen warning you that the"
Print"dimensional disterbances caused by the sproutoids may have caused the"
Print"bolonium walls to become unstable which may cause them to exert strange"
Print"forces over the occupants of the vessel"
Print"Will there be any end to your ordeal?"  
Print"Does anyone care?"
Print""
Flip 
Flip

End Function 