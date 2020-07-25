;Monster

;go into graphics mode
Graphics 640,480

Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;enable double buffering
SetBuffer BackBuffer()

A$="Beware, Beware! Monster word attack !"
length=Len(A$)


Type monst
Field x
Field y
Field dx
Field dy
Field c$
Field red=255
End Type

For mult=1 To 8
For loop=1 To length 
abc.monst=New monst
abc\x=0-loop*8
abc\y=50*mult
abc\dx=1
abc\dy=mult
abc\c$=Mid$(A$,loop,1)
Next  
Next

;loop until ESC hit...
While Not KeyDown(1)
Cls 
For abc.monst =Each monst

Color abc\y,0,200
Text abc\x,abc\y,abc\c$

abc\x=abc\x+abc\dx
abc\y=abc\y+abc\dy

abc\dx=confine(abc\x,abc\dx,0,width)
abc\dy=confine(abc\y,abc\dy,0,height)

Next
Flip 
Wend
End


;Stops x# from exceeding min and max values by ensuring direction of velocity is towards the boundary if object leaves the edge.
;Used to stop things leaving the edge of the screen.
Function confine#(x#,dx#,min,max)
If x#<min And dx#<0 Then dx#=-dx#
If x#>max And dx#>0 Then dx#=-dx#
Return dx#
End Function