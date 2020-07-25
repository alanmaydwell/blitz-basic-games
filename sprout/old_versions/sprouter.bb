;The Sprouter Limits


;go into graphics mode
Graphics 640,480

;Stores width and height of screen for scaling purposes
width=GraphicsWidth()
height=GraphicsHeight()

;enable double buffering
SetBuffer BackBuffer()

;Tells Blitz Basic  to handle centring of images
AutoMidHandle True  

rots=36 ;number of orientations for ship and sprouts
Dim playership(rots) ;stores ship images in different orientations
Dim sproutoid(rots); stores sproutoids images in different orientations
Dim trig#(rots,1) ;Stores Sin and Cos look-up table.

bpcount=0;Number of bullets launched at one time
maxbpcount=4;Maximum number of symultaneous bullets

nsprout=6; number of sproutoids

;Rotates the ship image and stores separate playership in playership(loop)
;Does similar job for the sprouts!
;Also creates sin and cos look up table
shipimg=LoadImage("ship.bmp")
sproutimg=LoadImage("midsprout.bmp")

snd_blip=LoadSound("blip.wav")
snd_shoot=LoadSound("shoot.wav")


Print"Calculating ..."
For loop=0 To rots-1
angle=loop*360/rots
playership(loop)=CopyImage(shipimg)
sproutoid(loop)=CopyImage(sproutimg)
RotateImage playership(loop),angle
RotateImage sproutoid(loop),angle
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
Print loop+" / " + (rots-1) +" Angle, " +angle +" Sin, " +trig#(loop,0) +" Cos, " +trig#(loop,1)
Next

;ship type
Type ship
Field x	;x coord
Field y	;y coord
Field dx# ;x velocity
Field dy# ;y velocity
Field o	;orientation
End Type

;Set player ship initial conditions
player.ship=New ship
player\x=width/2
player\y=height/2
player\dx#=0
player\dy#=0
player\o=0

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
End Type

;Generate Sproutoids
For loop=1 To nsprout
sprout.blob=New blob 
sprout\x=100*loop
sprout\y=Rnd(50,(height-50))
sprout\dx#=Rnd(-2,2)
sprout\dy#=Rnd(-2,2)
sprout\o=Rnd(rots)
Next

greet();welcome screen

;Main loop. Keeps going until escape pressed
While Not KeyDown(1)

Cls

;Draws border to screen with colour that varies with ship coordinates
Color 255,player\x,player\y
Rect 0,0,width,height,0

;If z pressed rotate the ship anticlockwise
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
pb\life#=700
PlaySound snd_shoot
End If

;Update missiles if launched
If bpcount<>0 Then
For pb.bullet = Each bullet
Color 255,pb\life#/3,0
Oval pb\x,pb\y,4,4
pb\x=pb\x+pb\dx#
pb\y=pb\y+pb\dy#
pb\life#=pb\life#-(Sqr(pb\dx#^2+pb\dy#^2))

;Confines bullets to screen
If pb\x<0 Then pb\x=0:pb\dx#=-pb\dx#
If pb\y<0 Then pb\y=0:pb\dy#=-pb\dy#
If pb\x>width Then pb\x=width:pb\dx#=-pb\dx#
If pb\y>height Then pb\y=height:pb\dy#=-pb\dy#
If pb\life#<=0 Then Delete pb:bpcount=bpcount-1; kill missile at edge of range
Next


 
End If 

;Draw sproutoids
DrawImage playership(player\o),player\x,player\y

For sprout.blob = Each blob
DrawImage(sproutoid(sprout\o),sprout\x,sprout\y)

;Detects collision between sproutoid and ship
If ImagesCollide(playership(player\o),player\x,player\y,0,sproutoid(sprout\o),sprout\x,sprout\y,0)
PlaySound (snd_blip)
player\dx#=-player\dx#
player\dy#=-player\dy#
End If

sprout\o=sprout\o+1
If sprout\o>=rots Then sprout\o=0
sprout\x=sprout\x+sprout\dx#
sprout\y=sprout\y+sprout\dy#
If sprout\x<0 Then sprout\x=0: sprout\dx#=-sprout\dx#
If sprout\x>width Then sprout\x=width: sprout\dx#=-sprout\dx#
If sprout\y<0 Then sprout\y=0: sprout\dy#=-sprout\dy#
If sprout\y>height Then sprout\y=height: sprout\dy#=-sprout\dy#
Next


Flip 
Wend

Function greet()
Cls:Flip 
Color 100,100,500
Print"THE SPROUTER LIMITS"
Print""
Print"X = Right"
Print"Z = Left"
Print"Right-Shift = Thrust"
Print"Enter = Fire"
Print""
Print"Press space bar to continue:"
For loop=0 To 12
Print ""
Next

Repeat
Until KeyDown(57)
End Function 