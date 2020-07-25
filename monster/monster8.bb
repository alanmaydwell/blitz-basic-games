;Monster

;go into graphics mode
Graphics 640,480

shipimg=LoadImage("data\tippex_sm.bmp")
backimage=LoadImage("data\background.bmp")
fnt_txt=LoadFont("data\typistb.ttf",12,False,False,False)
SetFont fnt_txt

Global snd_shoot=LoadSound("data\splat.wav")
SoundVolume snd_shoot,.4  
Global snd_hit=LoadSound("data\budsplat2.wav")
Global snd_spawn=LoadSound("data\typebell.wav")


;Set some scaling parameters
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2

;enable double buffering
SetBuffer BackBuffer()

;player paramenters
Global player_x=midx
Global player_y=height-10
Global player_dx# ;x velocity
Global player_dy# ;y velocity

Global maxpbcount=8;maximum number of bullets
Global pbcount=0; Records number of bullets in exitence
Global range=300; bullet range

;bullet type
Type bullet
Field x,y
Field dx#,dy#
Field life#
End Type

;Define data type for monsters
Type monst
Field x
Field y
Field oldx
Field oldy
Field dx
Field dy	
Field c$	;Stores single character extracted from string
Field cp	;Character position in text
Field final	;flag for final character in text
Field mt	;sets the movement behaviour of text
End Type

;Marks locations of "dead" characters
Type grave
Field x
Field y
Field c$
End Type 


spawn_monster(2,1,2,2,1)

setship(player_x,player_y)
ClsColor 200,200,200

;Main Loop. Repeats unless ESC hit.

While Not KeyDown(1)
Cls
TileImage backimage


update_monsters()
control_ship()
update_ship()

plot_graves()		;marks locations of shot characters

DrawImage(shipimg,player_x,player_y)

;Action taken if bullets in existence
If pbcount<>0 Then
update_bullets()	;update bullets
hit()				;collision detection
End If  

Flip
Delay 100 
Wend
End


;**********************************************************
; Functions start here 
;**********************************************************


Function spawn_monster(m1,m2,m3,m4,m5)

For loop=0 To m1 
If loop<>0 Then create_monster("Minator",0,12*loop,8,1,1)
Next

For loop=0 To m2 
If loop<>0 Then create_monster("Centipede",0,loop*8,8,0,2)
Next 

For loop=0 To m3 
If loop<>0 Then create_monster("Harpy",loop*8,0,5,0,3)
Next 

For loop=0 To m4
If loop<>0 Then create_monster("Wisp",0,loop*11,2,2,4)
Next

For loop=0 To m5
If loop<>0 Then create_monster("Vampyre",loop,loop*60,2,6,5)
Next
PlaySound snd_spawn
End Function 

Function create_monster(A$,x,y,dx,dy,mt)
length=Len(A$)
For loop=1 To length 
abc.monst=New monst
abc\x=x
abc\y=y
abc\dx=dx
abc\dy=dy
abc\c$=Mid$(A$,loop,1)
abc\cp=loop
If loop =length Then 
abc\final=True
Else abc\final=False
abc\mt=mt
End If 
Next
End Function 


;Updates and plots the "monsters"
Function update_monsters()
nmonster=0
For abc.monst =Each monst
nmonster=nmonster+1
Color 0,0,0
;Color 255-abc\cp*6,255-abc\cp*6,250

;if only one character left in word then colour it red otherwise colour black.
If abc\final=True And abc\cp=1 Then Color 255,0,0 Else Color 0,0,0 
Text abc\x,abc\y,abc\c$

abc\oldx=abc\x
abc\oldy=abc\y

;updates the lead character
If abc\cp=1 Then

;Applies differnt types of movement for different types of monsters (\mt)
Select abc\mt
Case 1;default (simple reflection)

Case 2; centipede 
If abc\dy=8 Then abc\dy=0
If abc\dy=-8 And abc\y<(height-100) Then abc\dy=0
If (abc\x=0 Or abc\x=width) Then abc\dy=8
If abc\y=height Then abc\dy=-8

Case 3;bouncer
If abc\dy<20 Then abc\dy=abc\dy+1

Case 4;wobbler
x=Rnd(1,5)
If x=1 Then 
abc\dx=abc\dx+Rnd(-1,1)
abc\dy=abc\dy+Rnd(-1,1)
If Abs(abc\dx)>10 Then abc\dx=abc\dx*.8
If Abs(abc\dy)>10 Then abc\dy=abc\dy*.8
End If

Case 5;vampyre
If abc\x>player_x Then 
abc\dx=-1
Else abc\dx=1
End If 

 
End Select


;updates the lead character position and confines to screen
abc\dx=confine(abc\x,abc\dx,0,width)
abc\dy=confine(abc\y,abc\dy,0,height)
abc\x=abc\x+abc\dx
abc\y=abc\y+abc\dy

;updates the following characters, each of which takes the preceding character's coordinates
Else  
abc=Before abc; moves pointer forward to read the previous character's old coordinates
tempx=abc\oldx
tempy=abc\oldy
abc=After abc; restores pointer to current position
abc\x=tempx
abc\y=tempy
End If 
Next 
Text 10,10,nmonster
End Function

;Stops x# from exceeding min and max values by ensuring direction of velocity is towards the boundary if object leaves the edge.
;Used to stop things leaving the edge of the screen.
Function confine#(x#,dx#,min,max)
If x#<min And dx#<0 Then dx#=-dx#
If x#>max And dx#>0 Then dx#=-dx#
Return dx#
End Function


;Set up new player ship initial conditions
Function setship(x,y)
player_x=x
player_y=y
player_dx#=0
player_dy#=0
End Function

Function control_ship()
player_dx#=0:player_dy#=0

;If z pressed move ship left
If KeyDown(45) Then player_dx#=6 

;If x pressed move ship right
If KeyDown(44) Then player_dx#=-6

;If ; pressed then move ship up
If KeyDown(39) Then player_dy#=-6

;If . pressed then move ship right
If KeyDown(52) Then player_dy#=6

;If fire button pressed lauch missile if maximum number not already reached 
If KeyHit(28) And pbcount<maxpbcount Then
PlaySound snd_shoot
pbcount=pbcount+1 
pb.bullet=New bullet
pb\x=player_x+6
pb\y=player_y-4
pb\dx#=0
pb\dy#=-8
pb\life#=range
End If

End Function



Function update_ship()

;Stops ship from leaving edge of screen 
player_dx#=confine(player_x,player_dx#,0,width)
player_dy#=confine(player_y,player_dy#,0,height)

;Update ship position
player_x=player_x+player_dx#
player_y=player_y+player_dy#

End Function 


Function update_bullets()
For pb.bullet = Each bullet
Color 255,255,255
Oval pb\x,pb\y,5,5
pb\x=pb\x+pb\dx#
pb\y=pb\y+pb\dy#
pb\life#=pb\life#-(Sqr(pb\dx#^2+pb\dy#^2))

; kill missile at edge of range
If pb\life#<=0 Then Delete pb:pbcount=pbcount-1

Next
End Function 

;collision detection 
Function hit() 

;collision detection between word and bullet
For pb.bullet=Each bullet
For abc.monst=Each Monst


	If RectsOverlap(pb\x,pb\y,6,6,abc\x,abc\y,10,10) Then
	temp=abc\cp; temp stores the position of the character hit
	final=abc\final

	;If final character is hit (and it is not the only character) then the previous character is now designated the final character.  
	If final=True And abc\cp<>1 Then
	abc=Before abc
	abc\final=True
	abc=After abc
	End If  

	create_grave(abc\x,abc\y,abc\c$)
	PlaySound snd_hit
	Oval abc\x,abc\y,30,30 
	Delete abc ;deletes the hit character
	
	;If character other than final character was hit then following character is given new position number
	If final=False Then 
	abc=After abc ;moves pointer to next character
	abc\cp=temp ; renumbers the character just switched to
	End If 
	pb\life#=0; sets the bullet life to zero so it will be deleted by function update_bullets
	End If 

Next 
Next 
End Function

Function create_grave(x,y,c$)
dead.grave=New grave
dead\x=x
dead\y=y
dead\c$=c$
End Function

Function plot_graves()
Color 255,255,255
For dead.grave=Each grave
Text dead\x,dead\y,dead\c$
Next
End Function 