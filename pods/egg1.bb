;zobulise test

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;Load graphics
Global im_pod=LoadImage("data\redsq.bmp")
Global im_liz=LoadImage("data\redcro.bmp")
Global im_dia=LoadImage("data\diathing.bmp")

;enable double buffering
SetBuffer BackBuffer()


Type egg
Field x,y
Field dx,dy
Field status
Field targ
End Type 
npods=5

Type fly
Field x,y
Field dx,dy
Field status
End Type
Global nfly=2 
Dim bem.fly(nfly)

;loop until ESC hit...

set_pods(npods)
set_bem

While Not KeyDown(1)

Cls
update_pods
update_bem()
Flip 
Wend 
End 



Function set_pods(n)
For loop=0 To n
pod.egg=New egg
pod\x=Rnd(32,width-32)
pod\y=Rnd(32,height-32)
pod\dx=Rnd(-2,2)
pod\dy=Rnd(-2,2)
pod\status=0
Next 
End Function

Function update_pods()
For pod.egg = Each egg
If pod\status=0 Then
DrawImage im_pod,pod\x,pod\y
Else DrawImage im_liz,pod\x,pod\y
End If 

pod\dx=confine(pod\x,pod\dx,0,width)
pod\dy=confine(pod\y,pod\dy,0,height)

pod\x=pod\x+pod\dx
pod\y=pod\y+pod\dy
Next
End Function

Function set_bem()
For loop=0 To nfly
bem.fly(loop)=New fly
bem(loop)\x=Rnd(width)
bem(loop)\y=Rnd(height)
Next
End Function

Function update_bem()
For i=0 To nfly
DrawImage im_dia,bem(i)\x,bem(i)\y
Text bem(i)\x,bem(i)\y,i
Next
End Function  

Function confine(x,dx,min,max)
If x<min And dx<0 Then dx=-dx
If x>max And dx>0 Then dx=-dx
Return dx
End Function

;Determines nearest fly to specified coordinates
Function nearest(x,y)
target=-1:sep=10000
For i=0 To nfly
j=dist(x,y,bem(i)\x,bem(i)\y)
If j<sep Then
sep=j 
target=i
End If 
Next
Return target
End Function

;Calculates distance between two points
Function dist(x1,y1,x2,y2)
dx=x1-x2
dy=y1-y2
dist=Sqr(dx*dx+dy*dy)
Return dist 
End Function 