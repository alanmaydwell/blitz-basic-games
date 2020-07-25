;Bones

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2


;************************
;*** Laser Parameters ***
;************************
;
Global nbeams=0    ;Number of beams in existence
Global maxbeams=8  ;Maximum number of symaltaneous beams allowed
Dim lascol(5,2)    ;Sort of colour palette for laser
Global c=0         ;Colour index for laser beam (position of 1st coordinate in array)

;Stores primary and secondary rgb colour values in array 
Restore lcolourdata
For loop1=0 To 5
For loop2=0 To 2
Read num 
lascol(loop1,loop2)=num 
Next 
Next 

Type laser
Field y
Field x  ;Start point of beam
Field x2 ;end point of beam
Field xmax; maximum end point of beam
End Type 


;enable double buffering
SetBuffer BackBuffer()


;loop until ESC hit...
While Not KeyDown(1)
Cls 
Color 255,255,255
Plot MouseX(),MouseY()
If MouseHit(1) And nbeams<maxbeams Then shoot_laser(MouseX(),MouseY(),480)
update_laser()
Flip 
Wend
End 

;***********************
;*** Laser Functions ***
;***********************

Function shoot_laser(x,y,l)
nbeams=nbeams+1
pop.laser=New laser
pop\y=y
pop\x=x
pop\x2=x
pop\xmax=pop\x-l
End Function

Function update_laser()
For pop.laser = Each laser
Color lascol(c,0),lascol(c,1),lascol(c,2)
Line pop\x,pop\y,pop\x2,pop\y

;Adds speckles to beam
Color 0,0,0
For loop=0 To 10
x=Rnd(pop\x,pop\x2)
Line  x,pop\y,x+4,pop\y
;Plot pop\x,y
Next

;Lengthens beam if it has not reached full extension otherwise shortens it
If pop\x2>=pop\xmax Then 
pop\x2=pop\x2-6
pop\x=pop\x-2
Else pop\x=pop\x-12
End If 

;Deletes beam once length back to zero. Also shifts the beam colour
If pop\x<=pop\xmax Then 
Delete pop
nbeams=nbeams-1
c=(c+1)Mod 5 
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