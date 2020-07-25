Graphics 640,480

Global width=GraphicsWidth()
n=8
frame#=width/24
rad#=frame/2
dec#=frame/n
im_circs=CreateImage(frame#,frame#,n)




loop=1
Repeat
Print loop
SetBuffer ImageBuffer(im_circs,loop-1) 
Color 255,0,0
Oval 0,0,frame,frame,0
Color 0,0,255
k=loop-1
j=frame#-(loop*dec#)
Oval rad-j/2,rad-j/2,j,j,0
loop=loop+1
Until loop>n 

SetBuffer BackBuffer() 
Repeat
For loop=0 To n-1
Cls 
DrawImage im_circs,320,240,loop
Flip 
Next
Until KeyDown(1) 
WaitKey 
