;Explosion test

;go into graphics mode
Graphics 640,480
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2


;enable double buffering
SetBuffer BackBuffer()

;**********************
;*Explosion Parameters*
;**********************
Global expcol=0 ;explosion colour

Type expl
Field x
Field y
Field dx
Field dy
Field e ;lifetime of explosion
Field s	;Particle Size
Field c ;explosion colour
End Type


;loop until ESC hit...
While Not KeyDown(1)

Cls
Color 255,255,255
Plot MouseX(),MouseY()
If MouseHit(1) Then 
bang(MouseX(),MouseY(),40,50,1,expcol)
expcol=(expcol+1) Mod(6); Advances the explosion colour index
End If 

plotexp()
Flip 
Wend 
End 


;***************************
;*** Explosion Functions ***
;***************************
;
;Creates explosion centred on x,y made up of n particles 
;that will Last For l loops and are of size s and have colour index expcol.
Function bang(x,y,n,l,s,expcol)
For loop=1 To n
pop.expl=New expl
pop\s=s
pop\e=l
pop\x=x
pop\y=y
pop\c=expcol
Repeat 
pop\dx=Rnd(-3,3)
pop\dy=Rnd(-3,3)
Until pop\dx<>0 Or pop\dy<>0
Next 
End Function 

;Updates explosion
Function plotexp() 
For pop.expl = Each expl

;Selects colour and brightness
c=4*pop\e
Select pop\c
Case 0 
Color c,0,0
Case 1
Color c,c,0
Case 2
Color 0,c,0
Case 3
Color 0,c,c
Case 4
Color c,0,c
Case 5
Color c,c,c
End Select 

;plots particles and expands to new locations
Rect pop\x,pop\y,pop\s,pop\s
pop\x=pop\x+pop\dx
pop\y=pop\y+pop\dy
pop\e=pop\e-1
If pop\e<0 Then Delete pop 
Next
End Function 

 