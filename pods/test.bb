Graphics 640,480

Global width=GraphicsWidth()
n=8
frame#=width/24
rad#=frame/2
dec#=frame/n

loop=1
Repeat
Color 255,0,0
Oval (loop-1)*frame,0,frame,frame,0
Color 0,0,255
k=loop-1
j=frame#-(loop*dec#)
Oval ((loop-1)*frame#)+rad-j/2,rad-j/2,j,j,0
loop=loop+1
Until loop>n 

WaitKey
im_circs=CreateImage(32,32,10)
GrabImage(im_circs,0,0)

WaitKey
For loop=0 To n
Text 20,20,n
DrawImage im_circs,320,240,n
Next 
WaitKey 