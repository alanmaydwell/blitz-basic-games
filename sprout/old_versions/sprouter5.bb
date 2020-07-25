;The Sprouter Limits
;An asteroids type game.
;By Alan Maydwell
;Feb 2002


;go into graphics mode
Graphics 640,480

;Stores width and height of screen for scaling purposes
Global width=GraphicsWidth()
Global height=GraphicsHeight()

;enable double buffering
SetBuffer BackBuffer()

;Tells Blitz Basic  to handle centring of images
AutoMidHandle True  

Global rots=36 ;number of orientations for ship and sprouts
Dim playership(rots) ;stores ship images in different orientations
Dim b_sproutoid(rots); stores big sproutoid images in different orientations
Dim s_sproutoid(rots); stores small  sproutoid images in different orientations
Dim trig#(rots,1) ;Stores Sin and Cos look-up table.

;Load Graphics
shipimg=LoadImage("ship.bmp")
b_sproutimg=LoadImage("midsprout.bmp")
s_sproutimg=LoadImage("minisprout.bmp")

;Load Sounds
snd_blip=LoadSound("blip.wav")
snd_shoot=LoadSound("shoot.wav")
snd_exp=LoadSound("explosion.wav")
snd_bump=LoadSound("ow.wav")
snd_burp=LoadSound("burp.wav")

Global bpcount=0;records number of bullets launched at one time
maxbpcount=4;Maximum number of simultaneous bullets
nsprout=12; number of sproutoids
Global score=0 ;stores score


story()
Repeat
Until KeyDown(57) 

;Rotates the ship image and stores separate playership in playership(loop)
;Does similar job for the sprouts!
;Also creates sin and cos look up table
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
Field fromwho
End Type

;Sprouttype
Type blob
Field x	;x coord
Field y	;y coord
Field dx# ;x velocity
Field dy# ;y velocity
Field o  ;orientation
Field s ;size
End Type

While Not KeyDown(1); Outer loop
greet();welcome screen
score=0
level=1


;Set up player ship
lives=3
setship(width/2,height/2)


;Main loop. Keeps going until escape pressed
Repeat; Game in progress loop

;Generate Sproutoids (x,y,size)
For loop=1 To nsprout
newsprout(100*loop,Rnd(50,height-50),1)
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
player_dx#=player_dx#+(.1*trig#(player_o,0)) 
player_dy#=player_dy#-(.1*trig#(player_o,1))
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
If KeyHit(53) And bpcount<maxbpcount Then
bpcount=bpcount+1 
pb.bullet=New bullet
pb\x=player_x
pb\y=player_y
pb\dx#=player_dx#+8*trig#(player_o,0)
pb\dy#=player_dy#-8*trig#(player_o,1)
pb\life#=500
PlaySound snd_shoot
End If

;Update missiles if launched
If bpcount<>0 Then
For pb.bullet = Each bullet
Color 255,pb\life#/2,55
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
If pb\x<0 Then pb\x=0:pb\dx#=-pb\dx#
If pb\y<0 Then pb\y=0:pb\dy#=-pb\dy#
If pb\x>width Then pb\x=width:pb\dx#=-pb\dx#
If pb\y>height Then pb\y=height:pb\dy#=-pb\dy#

; kill missile at edge of range
If pb\life#<=0 Then Delete pb:bpcount=bpcount-1
Next

End If 

;Draw sproutoids
DrawImage playership(player_o),player_x,player_y

sproutcount=0
For sprout.blob = Each blob
Gosub drawsprouts 
sproutcount=sproutcount+1

;Detects collision between sproutoid and ship
If sprout\s=1 And ImagesCollide(playership(player_o),player_x,player_y,0,b_sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_blip)
i=player_e*cs#
Color 150,i,i
Oval player_x-18,player_y-18,36,36,0
player_dx#=-player_dx#
player_dy#=-player_dy#
player_e=player_e-1
End If

If sprout\s=0 And ImagesCollide(playership(player_o),player_x,player_y,0,s_sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_bump)
i=player_e*cs#
Color 150,i,i
Oval player_x-18,player_y-18,36,36,0
player_dx#=-player_dx#
player_dy#=-player_dy#
player_e=player_e-1
End If

If player_e<=0 Then

;draw circular explosion centred on ship
For loop=1 To 512 Step 8
shift=loop/2
shift2=loop/4
Color 255,255-shift,shift2
Oval player_x-shift,player_y-shift,loop,loop,0
Color 0,0,0
Oval player_x-shift2,player_y-shift2,shift,shift,0
Flip
Next
lives=lives-1
player_e=20;Bodge to stop the explosion repeating for each remaining sprout after final life used. (If not final life player_e will be reset by line below).
If lives >0 Then Gosub restoreship:Exit ;Resets ship if lives remaining. Wait till coast is centre of screen is clear.
End If 
Gosub updatesprouts
Next
Text 10,10,"Sprouts:"+ sproutcount+"  Level:"+level +"  Energy:"+player_e +"  Lives:"+ lives+"  Score:"+score
Flip 
Until sproutcount=0 Or lives<=0 Or KeyDown(1)  
 
level=level+1
clear();deletes sproutoids and bullets

Until lives<=0 Or KeyDown(1)
;game now over


Wend  
End 
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
sprout\dx#=Rnd(-2,2)
sprout\dy#=Rnd(-2,2)
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
player_e=30
cs#=255/player_e
End Function

;Checks coast is clear before starting new life in centre of screen
.restoreship
x=(width/2)-100
y=(height/2)-100
Repeat
safe=True
Cls
For sprout.blob = Each blob
If sprout\s=1 And ImageRectOverlap(b_sproutoid(sprout\o),sprout\x,sprout\y,x,y,200,200) Then safe=False
If sprout\s=0 And ImageRectOverlap(s_sproutoid(sprout\o),sprout\x,sprout\y,x,y,200,200) Then safe=False
Gosub updatesprouts
Gosub drawsprouts
Next
Flip
Until safe=True
setship(320,240)
Return

.updatesprouts
sprout\o=sprout\o+1
If sprout\o=rots Then sprout\o=0
sprout\x=sprout\x+sprout\dx#
sprout\y=sprout\y+sprout\dy#
If sprout\x<0 Then sprout\x=0: sprout\dx#=-sprout\dx#
If sprout\x>width Then sprout\x=width: sprout\dx#=-sprout\dx#
If sprout\y<0 Then sprout\y=0: sprout\dy#=-sprout\dy#
If sprout\y>height Then sprout\y=height: sprout\dy#=-sprout\dy#
Return

;draws the sprouts 
.drawsprouts
If sprout\s=1 Then DrawImage(b_sproutoid(sprout\o),sprout\x,sprout\y)
If sprout\s=0 Then DrawImage(s_sproutoid(sprout\o),sprout\x,sprout\y)
Return

End 
End 

;-----------------------------------------------
;Welcome Screen
Function greet()
y1=0
y2=0

SetBuffer BackBuffer()
ymax1=ImageHeight(b_sproutoid(1))
ymax2=ImageHeight(s_sproutoid(1))

;Y coordinates for text. Scaled to screen.
top=height*.1
h1=height*.3
h2=height*.35
h3=height*.4
h4=height*.45
h5=height*.7
h6=height*.9
;x coordinates for centre of each text line
x=width/2


Repeat 
Cls 
TileBlock s_sproutoid(1),0,y2
TileImage b_sproutoid(1),0,y1
i=4*y1;colour intensity

Color 255-i,255-i,255
Text x,top,"THE SPROUTER LIMITS",True,True 

Color i,255,i 
Text x,h1,"X = Right",True,True 
Text x,h2,"Z = Left",True,True 
Text x,h3,"Right-Shift = Thrust",True,True 
Text x,h4,"Enter = Fire",True,True
Color i,i,i
Text x,h5,"Last Score:"+score,True,True 
Color 255,0,0
Rect width/3,h6-10,215,20 
Color 255,i,i
Text x,h6,"Press space bar to start",True,True
y1=y1+2:If y1>ymax1 Then y1=0 
y2=y2+1:If y2>ymax2 Then y2=0 

Flip
Until KeyDown(57)

End Function

Function story()
Text width/2,20,"THE SPROUTS ARE REVOLTING!",True,True
Print""
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
Color 255,0,100
Print"Press Space to continue"

Flip:Flip

End Function 