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

;Load Sound
snd_blip=LoadSound("blip.wav")
snd_shoot=LoadSound("shoot.wav")
snd_exp=LoadSound("explosion.wav")

bpcount=0;records number of bullets launched at one time
maxbpcount=4;Maximum number of simultaneous bullets
nsprout=12; number of sproutoids


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


;ship type
Type ship
Global Field x	;x coord
Global Field y	;y coord
Global Field dx# ;x velocity
Global Field dy# ;y velocity
Global Field o	;orientation
Global Field e ;energy
Global Field d ;alive or dead?
End Type


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

greet();welcome screen
score=0
level=1

;Main loop. Keeps going until escape pressed
While Not KeyDown(1)


;Generate Sproutoids (x,y,size)
For loop=1 To nsprout
newsprout(100*loop,Rnd(50,height-50),1)
Next

;Set up player ship
setship(width/2,height/2)


Repeat

Cls

;Draws border to screen with colour that varies with ship coordinates
Color 255,player\x,player\y
Rect 0,0,width,height,0

If z pressed rotate the ship anticlockwise
If KeyDown(45) Then
player\o=player\o+1 
If player\o=rots Then player\o=0
End If

;If x pressed rotate the ship clockwise
If KeyDown(44) Then
player\o=player\o-1
If player\o=-1 Then player\o=rots-1
End If

;if r-shift pressed then accelerate ship
If KeyDown(54) Then
player\dx#=player\dx#+(.1*trig#(player\o,0)) 
player\dy#=player\dy#-(.1*trig#(player\o,1))
End If

;friction. Automatically slows the ship
player\dx#=player\dx#*.999
player\dy#=player\dy#*.999


;Update ship position
player\x=player\x+player\dx#
player\y=player\y+player\dy#

;Stops ship from leaving edge of screen
If player\x<0 Then player\x=0: player\dx#=-player\dx#
If player\x>width Then player\x=width: player\dx#=-player\dx#
If player\y<0 Then player\y=0: player\dy#=-player\dy#
If player\y>height Then player\y=height: player\dy#=-player\dy#

;If fire button pressed lauch missile and set missile speeds
If KeyHit(28) And bpcount<maxbpcount Then
bpcount=bpcount+1 
pb.bullet=New bullet
pb\x=player\x
pb\y=player\y
pb\dx#=player\dx#+8*trig#(player\o,0)
pb\dy#=player\dy#-8*trig#(player\o,1)
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
PlaySound snd_exp
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
DrawImage playership(player\o),player\x,player\y

sproutcount=0
For sprout.blob = Each blob
If sprout\s=1 Then DrawImage(b_sproutoid(sprout\o),sprout\x,sprout\y)
If sprout\s=0 Then DrawImage(s_sproutoid(sprout\o),sprout\x,sprout\y)
sproutcount=sproutcount+1 

;Detects collision between sproutoid and ship
If sprout\s=1 And ImagesCollide(playership(player\o),player\x,player\y,0,b_sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_blip)
player\dx#=-player\dx#
player\dy#=-player\dy#
player\e=player\e-1
End If

If sprout\s=0 And ImagesCollide(playership(player\o),player\x,player\y,0,s_sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_blip)
player\dx#=-player\dx#
player\dy#=-player\dy#
player\e=player\e-1
End If

If player\e=0 Then player\d=True


sprout\o=sprout\o+1
If sprout\o>=rots Then sprout\o=0
sprout\x=sprout\x+sprout\dx#
sprout\y=sprout\y+sprout\dy#
If sprout\x<0 Then sprout\x=0: sprout\dx#=-sprout\dx#
If sprout\x>width Then sprout\x=width: sprout\dx#=-sprout\dx#
If sprout\y<0 Then sprout\y=0: sprout\dy#=-sprout\dy#
If sprout\y>height Then sprout\y=height: sprout\dy#=-sprout\dy#
Next
Text 10,10,"Sprouts:"+ sproutcount+"   Score:"+score+"   Level:"+level +" Energy:"+player\e
Flip 
Until sproutcount=0 Or KeyDown(1) 
level=level+1

Wend

End 

;Set up player ship initial conditions
Function setship(x,y)
player.ship=New ship
player\x=x
player\y=y
player\dx#=0
player\dy#=0
player\o=0
player\e=1000
player\d=False
End Function 

;Generates new sproutoids
Function newsprout(x,y,s)
sprout.blob=New blob 
sprout\x=x
sprout\y=y
sprout\dx#=Rnd(-2,2)
sprout\dy#=Rnd(-2,2)
sprout\o=Rnd(rots)
sprout\s=s
End Function 

;-----------------------------------------------
;Welcome Screen
Function greet()
y=0

SetBuffer BackBuffer()

Repeat 
Cls 
TileBlock s_sproutoid(0),0,y
TileImage b_sproutoid(0),0,y*2

Color 255-y*4,255-y*4,255
Text width/2,120,"THE SPROUTER LIMITS",True,True 

Color 4*y,255,4*y 
Text width/2,160,"X = Right",True,True 
Text width/2,180,"Z = Left",True,True 
Text width/2,200,"Right-Shift = Thrust",True,True 
Text width/2,220,"Enter = Fire",True,True 

Color 255,0,0
Rect width/3,430,215,20 
Color 255,255-4*y,255-4*y
Text width/2,height-40,"Press space bar to start",True,True
y=y+1:If y=64 Then y=0 

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
End Function 