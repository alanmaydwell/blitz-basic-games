;The Sprouter Limits


;go into graphics mode
Graphics 640,480

;enable double buffering
SetBuffer BackBuffer()

AutoMidHandle True  ;Tells Blitz Basic  to handle centring of images


rots=64 ;number of orientations for ship
Dim frames(rots)
Dim trig#(rots,1)

;Ship Coordinates and speeds
x=320
y=240
dx#=0
dy#=0

;missile coordinates and speeds
bx=320
by=240
dbx#=0
dby#=0
range=1000
shot=False; is missile in flight?



ship=LoadImage("ship.bmp")

;Rotates the image and stores separate frames in frames(loop)
Print"Calculating ..."
For loop=0 To rots-1
angle=loop*360/rots
frames(loop)=CopyImage(ship)
RotateImage frames(loop),angle
Print loop+" / " + (rots-1)
trig#(loop,0)=Sin(angle)
trig#(loop,1)=Cos(angle)
Next



;Displays each image in turn to give a rotating image
;loop until ESC hit...

p=0
While Not KeyDown(1)
Cls


;If z pressed rotate the ship anticlockwise
If KeyDown(45) Then
p=p+1 
If p=rots Then p=0
End If

;If x pressed rotate the ship clockwise
If KeyDown(44) Then
p=p-1
If p=-1 Then p=rots-1
End If

;if r-shift pressed then accelerate ship
If KeyDown(54) Then
dx#=dx#+(.3*trig#(p,0))
dy#=dy#-(.3*trig#(p,1))
End If

x=x+dx#
y=y+dy#

DrawImage(frames(p),x,y)

;If fire button pressed lauch missile and set missile speeds
If KeyHit(28) And shot=False Then 
shot=True
energy=range
bx=x: by=y
dbx#=dx#+8*trig#(p,0)
dby#=dy#-8*trig#(p,1)
End If

;Update missile if launched
If shot=True Then
Color 100+Rnd(155),100,0
Oval bx,by,10,10
bx=bx+dbx#
by=by+dby#
energy=energy-(Sqr(dbx#^2+dby#^2))
If bx<0 Then bx=0:dbx#=-dbx#
If bx>640 Then bx=640:dbx#=-dbx#
If by<0 Then by=0:dby#=-dby#
If by>480 Then by=480:dby#=-dby#

If energy<=0 Then shot=False; kill missile at edge of range
End If


;stops ship leaving screen egdges 
If x<0 Then
x=0
dx#=-dx#
End If

If x>640 Then 
x=640
dx#=-dx#
End If

If y<0 Then
y=0
dy#=-dy#
End If

If y>480 Then
y=480
dy#=-dy#
End If


;VWait; Wait VBL

Flip 
Wend
End