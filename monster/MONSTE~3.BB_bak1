;Monster
Global hit=0 
;go into graphics mode
Graphics 640,480

;Load Graphics
shipimg=LoadImage("data\tippex_sm.bmp")
Global backimage=LoadImage("data\liner.jpg")
Global typeimg=LoadImage("data\type.bmp")
Global eltypeimg=LoadImage("data\writer.bmp")

;Load Fonts
Global fnt_txt=LoadFont("data\typist.ttf",14,False,False,False)
Global fnt_txt_big=LoadFont("data\typist.ttf",18,False,False,False)
Global fnt_cut=LoadFont("data\cut-n-paste.ttf",64,False,False,False)

;Load Sounds
Global snd_shoot=LoadSound("data\splat.wav")
SoundVolume snd_shoot,.4  
Global snd_hit=LoadSound("data\budsplat2.wav")
Global snd_spawn=LoadSound("data\typebell.wav")
Global snd_boing=LoadSound("data\banana.wav")
SoundVolume snd_boing,.6 
Global snd_arrgh=LoadSound("data\arrgh.wav")
Global snd_areyou=LoadSound("data\are_sure.wav")
Global snd_excel=LoadSound("data\24.wav")
Global snd_dead=LoadSound("data\skunk.wav")
Global chn_aargh=PlaySound(snd_arrgh)
Global snd_clap=LoadSound("data\clap.wav")

;Set some scaling parameters
Global width=GraphicsWidth()
Global height=GraphicsHeight()
Global midx=width/2
Global midy=height/2
shipwidth=ImageWidth(shipimg)    ; width of ship image
shipheight=ImageHeight(shipimg)	 ; height of ship image
Global shipxmax=width-shipwidth	 ; maximum x-coord of ship
Global shipymax=height-shipheight; maximum y-coord of ship

;Sets some horizontal coordinates and stores in H(n)
steps#=20
Dim H(steps#)
For loop#=1.00 To steps#
H(loop#)=(loop#/steps#)*height
Next

;Scrolling Parameters 
background_y=0			;Y position for background scroll
Global scrollspeed=2	;Scroll speed

;player paramenters
Global player_x=-50 
Global player_y=height-100
Global player_dx#   ; x velocity
Global player_dy#   ; y velocity
Global maxpbcount=8 ; maximum number of bullets
Global pbcount=0	; Records number of bullets in exitence
Global range=800	; bullet range
Global energy=255  	; Ship energy
Global score=0		; score

;highscore parameters
Global ns=7		  ; Number of entries (-1) to be stored in the High Score Table. 
Dim hst(ns)		  ; stores scores
Dim hsnames$(ns,1); Stores names. (n,0) holds names, (n,1) holds verson of (n,0) with spaces replaced by dashes. (n,0) used for highscore table, (n,1) used for monsters created from table.


;Misc parameters
Global level=1		  ; Stores level value
Global levelend=False ; Has level ended
Global c=0			  ;	colour cycling variable for into screen. Also used to move High Score Table up and down.

;Red, Green, Blue parameters for "monsters". Black in game but white for into screen
Global r
Global b
Global g

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
Field oldx#
Field oldy#
Field dx#
Field dy#	
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

;enable double buffering
SetBuffer BackBuffer()
set_highscores(ns);	Fills highscore table with initial names

Repeat;outer loop

;Intro Screen

greet(fnt_cut,fnt_txt)
level=1
levelend=False
score=0
energy=255;  set ship energy to max at start of game.

;Main loop for game in progress
Repeat 
SetFont fnt_txt
player_x=midx
player_y=height-100
set_monsters()
setship(player_x,player_y)
counter=0
hurry$="Z"
r=0:g=0:b=0; set colour values for words
Cls 
list_monsters()
Flip 
Delay 1500

;Main Loop for level in progress. Repeats unless ESC hit.
While Not KeyDown(1) Or levelend=True 
Cls
TileImage backimage,0,background_y
background_y=background_y+scrollspeed Mod(98)
Color 0,0,255
SetFont fnt_cut 
Text 0,-20,score,0,0
SetFont fnt_txt_big
Text 260,2,"Energy:",0,0
Color 255,energy,0
Text 258,0,"Energy:",0,0
Rect midx,5,energy,10,True
Color 0,0,0
Rect midx,5,255,10,False
SetFont fnt_txt

;"hurry-up system". Generates new monster every after 1600 loops and then every 800.
If counter > 1600 Then 
counter=800 
create_monster(hurry$,midx,0,0,0,6)
create_monster(hurry$,0,0,0,0,6)
create_monster(Hurry$,width,0,0,0,6)
create_monster(Hurry$,0,midy,0,0,6)
create_monster(Hurry$,width,midy,0,0,6)
If Len(hurry$)<8 Then hurry$=hurry$+"z"
PlaySound snd_spawn
End If 

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
counter=counter+1
Wend
;End of loop for level in progress

Delay 400
clearup() 
ClsColor 0,0,200
SetFont fnt_cut
If energy>0
Color 200,200,0
Cls
PlaySound snd_excel
Text midx,midy,"Level "+level +" Complete",True,True
Flip 
Delay 2000
level=level+1
End If
levelend=False
Until energy<=0 Or KeyDown(1) 
;end of game in prgress loop

game_over()

Until KeyDown(1)
End 


;**********************************************************
; Functions start here 
;**********************************************************

Function set_monsters()
Select level 
Case 1
create_monster("Welcome",312,0,-8,0,2)
create_monster("To",384,0,-8,0,2)
create_monster("Level",408,0,-8,0,2)
create_monster("One!",448,0,-8,0,2)
Case 2
create_monster("Charge!",0,0,-8,6,1)
create_monster("Charge!",-40,0,-8,6,1)
create_monster("Charge!",-80,0,-8,6,1)
create_monster("Charge!",-120,0,-8,6,1)
Case 3
create_monster("Wobble",0,0,-8,0,4)
create_monster("Wibble",-40,0,-8,0,4)
create_monster("Dither",-80,0,-8,0,4)
create_monster("Amble",-120,0,-8,0,4)
Case 4
create_monster("Boing!",0,0,-8,0,3)
create_monster("Woooooo!",-80,0,-8,0,3)
create_monster("Woooooooo!",-120,0,-8,0,3)
Case 5
create_monster("Cooee!",0,0,-8,8,5)
create_monster("Me-Me-Me!",-40,0,-8,8,5)
create_monster("Gotcha",-80,0,-8,8,5)
create_monster("Zoom",-120,0,-8,8,5)
Case 6;(Words taken from Highscore table)
create_monster(hsnames$(0,1),0,0,-8,8,1)
create_monster(hsnames$(1,1),-40,0,-8,8,1)
create_monster(hsnames$(2,1),-80,0,-8,8,1)
create_monster(hsnames$(3,1),-120,0,-8,8,1)
create_monster(hsnames$(4,1),-160,0,-8,8,1)

Default;Generates number of monsters stored in "total" chosen randomly from the 6 available types. Number for each type stored in m1,m2,m3 etc.
total=8
m1=m2=m3=m4=m5=m6=0
Repeat
c=Rnd(0,total):If c=0 And total <>1 Then m1=m1+1:total=total-1
c=Rnd(0,total):If c=0 And total <>1 Then m2=m2+1:total=total-1
c=Rnd(0,total):If c=0 And total <>1 Then m3=m3+1:total=total-1
c=Rnd(0,total):If c=0 And total <>1 Then m4=m4+1:total=total-1
c=Rnd(0,total):If c=0 And total <>1 Then m5=m5+1:total=total-1
c=Rnd(0,total):If c=0 And total <>1 Then m6=m6+1:total=total-1
Until total=1

spawn_monster(m1,m2,m3,m4,m5,m6)
End Select
PlaySound snd_spawn
End Function 

Function spawn_monster(m1,m2,m3,m4,m5,m6)

For loop=0 To m1 
If loop<>0 Then create_monster("Minator",-(88*loop),0,6,1,1)
Next

For loop=0 To m2 
If loop<>0 Then create_monster("Centipede",-(200*loop),0,8,0,2)
Next 

For loop=0 To m3 
If loop<>0 Then create_monster("Harpy",loop*16,0,2,0,3)
Next 

For loop=0 To m4
If loop<>0 Then create_monster("Wisp",0,loop*11,2,2,4)
Next

For loop=0 To m5
If loop<>0 Then create_monster("Vampyre",loop,loop*60,2,6,5)
Next

For loop=0 To m6
If loop<>0 Then create_monster(hsnames$(loop,1),loop,loop*60,6,6,1)
Next

End Function 

Function create_monster(A$,x,y,dx#,dy#,mt)
length=Len(A$)
For loop=1 To length 
abc.monst=New monst
abc\x=x
abc\y=y
abc\dx#=dx#
abc\dy#=dy#
abc\c$=Mid$(A$,loop,1)
abc\cp=loop
If loop =length Then 
abc\final=True
Else abc\final=False
abc\mt=mt
End If 
Next
End Function 

;List monsters. Just lists the text of each monsters on the screen for the start of each level
Function list_monsters()
TileImage backimage,0,0
Color 0,0,0
SetFont fnt_cut
Text midx,h(1),"You will now face:",1,1
SetFont fnt_txt_big
a$=""
y=70
Print
Print 
For abc.monst =Each monst
a$=a$+abc\c$
If abc\final=True Then Text(midx,y,a$,1,1):a$="":y=y+14
Next
End Function 

;Updates and plots the "monsters"
Function update_monsters()
nmonster=0

For abc.monst =Each monst
nmonster=nmonster+1
;Color 255-abc\cp*6,255-abc\cp*6,250

;if only one character left in word then colour it red and turn it to vampyre (homing) otherwise colour black.
If abc\final=True And abc\cp=1 Then Color 255,0,0:abc\mt=6 Else Color r,g,b 


Text abc\x,abc\y,abc\c$; Plots the character

;Collision detection between character and ship
;Rect player_x,player_y,15,36
If RectsOverlap(player_x,player_y,15,36,abc\x,abc\y,14,14) Then
If ChannelPlaying(chn_aargh)=flase PlaySound snd_arrgh; Makes contact sound if not already playing
energy=energy-4
If energy<=0 Then levelend=True
End If


abc\oldx#=abc\x
abc\oldy#=abc\y

;updates the lead character
If abc\cp=1 Then

;Applies differnt types of movement for different types of monsters (\mt)
Select abc\mt
Case 1;default (simple reflection)

Case 2; centipede 
If abc\dy#=8 Then abc\dy#=0
If abc\dy#=-8 And abc\y<player_y Then abc\dy#=0
If (abc\x=0 Or abc\x=width) Then abc\dy#=8
If abc\y=height Then abc\dy#=-8

Case 3;bouncer
If abc\dy#<20 Then abc\dy#=abc\dy#+.5

Case 4;wobbler
x=Rnd(1,5)
If x=1 Then 
abc\dx#=abc\dx#+Rnd(-1.0,1.0)
abc\dy#=abc\dy#+Rnd(-1.0,1.0)
If Abs(abc\dx#)>10 Then abc\dx#=abc\dx#*.8
If Abs(abc\dy#)>10 Then abc\dy#=abc\dy#*.8
End If

Case 5;vampyre
If abc\x>player_x Then 
abc\dx#=-2
Else abc\dx#=2
End If 
If abc\dy#=0 Then abc\dy#=4

Case 6;homer
If abc\x>player_x Then 
abc\dx#=-3
Else abc\dx#=3
End If 
If abc\y>player_y Then 
abc\dy#=-3
Else abc\dy#=3
End If 

Default 

End Select

;updates the lead character position and confines to screen
abc\dx#=confine(abc\x,abc\dx#,0,width)
abc\dy#=confine(abc\y,abc\dy#,0,height)
abc\x=abc\x+abc\dx#
abc\y=abc\y+abc\dy#

;updates the following characters, each of which takes the preceding character's coordinates
Else  
abc=Before abc; moves pointer forward to read the previous character's old coordinates
tempx=abc\oldx#
tempy=abc\oldy#
abc=After abc; restores pointer to current position
abc\x=tempx
abc\y=tempy
End If 
Next 
;Text 10,10,nmonster
If nmonster=0 Then levelend=True 
End Function

;Stops x# from exceeding min and max values by ensuring direction of velocity is towards the boundary if object leaves the edge.
;Used to stop things leaving the edge of the screen.
Function confine#(x#,dx#,min,max)
If x#<min And dx#<0 Then dx#=-dx#:PlaySound snd_boing
If x#>max And dx#>0 Then dx#=-dx#:PlaySound snd_boing
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
player_dx#=0
player_dy#=0

;If z or joystick pressed move ship left
If KeyDown(45) Or JoyXDir()=1 Then  player_dx#=8 

;If x or joystick pressed move ship right
If KeyDown(44) Or JoyXDir()=-1 Then player_dx#=-8

;If ; pressed then move ship up
If KeyDown(39) Or JoyYDir()=-1 Then player_dy#=-8

;If . pressed then move ship right
If KeyDown(52) Or JoyYDir()=1 Then player_dy#=8

;If fire button pressed lauch missile if maximum number not already reached 
If (KeyHit(28) Or GetJoy()>0) And pbcount<maxpbcount Then
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
player_dx#=confine(player_x,player_dx#,0,shipxmax)
player_dy#=confine(player_y,player_dy#,0,shipymax)

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


	If RectsOverlap(pb\x,pb\y,6,6,abc\x,abc\y,14,14) Then
	temp=abc\cp; temp stores the position of the character hit
	final=abc\final
	score=score+1; increases score

	;If final character is hit (and it is not the only character) then the previous character is now designated the final character.  
	If final=True And abc\cp<>1 Then
	abc=Before abc
	abc\final=True
	abc=After abc
	End If  

	create_grave(abc\x,abc\y,abc\c$); Creates fixed version of character at the location where it was shot.
	PlaySound snd_hit
	Oval abc\x,abc\y,40,40 
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

;increases energy if ship over grave.
If RectsOverlap(player_x,player_y,15,36,dead\x,dead\y,14,14) And energy <255 Then
energy=energy+1
End If 

dead\y=dead\y+scrollspeed;moves each grave in sync with screen scroll
If dead\y>height Then Delete dead; delete grave if it falls off bottom of screen
Next

End Function 

;Intro Screen
Function greet(fnt_cut,fnt_txt)
player_y=H(16) 
player_x=-50; moves player coordinates off-screen during into screen to prevent collision detection noise
SetBuffer BackBuffer()
r=255:g=255:b=100; set colour values for words 

create_monster("A  g a m e  b y  A l a n  M a y d w e l l",width+250,H(3),-4,0,2)
create_monster("K E Y S",width+150,H(5),-4,0,2)
create_monster("Z  -  L e f t",width+150,H(6),-4,0,2)
create_monster("X  -  R i g h t",width+150,H(7),-4,0,2)
create_monster(":  -  U p",width+150,H(8),-4,0,2)
create_monster(".  -  D o w n",width+150,H(9),-4,0,2)
create_monster("R e t u r n  -  F i r e",width+150,H(10),-4,0,2)
create_monster("O r  u s e  a  j o y s t i c k", width+150,H(11),-4,0,2)
create_monster("P r e s s  E S C  t o  Q u i t", width+450,H(13),-4,0,2)
create_monster("P r e s s  S p a c e  t o  S t a r t",width+500,H(16),-4,0,2)
create_monster("O r  P r e s s  f i r e  b u t t o n  t o  s t a r t",-500,H(18),4,0,2)


ClsColor 255,255,255
c=0:dc=1

While Not KeyDown(57) Or GetJoy()>0
If KeyDown(1) End; quit game if ESC pressed
Cls
SetFont fnt_cut
Color c,0,c
ClsColor 255,255-c,255-c
DrawImage typeimg,-16,30
Text midx,H(1),"war of words",True,True
SetFont fnt_txt
display_highscores(ns)
update_monsters()
Flip
c=(c+dc)
If c=0 Or c=255 Then dc=dc*-1
Wend
PlaySound snd_areyou 
clearup()
PlaySound snd_areyou
ClsColor 255,255,0
Cls
TileImage backimage 
SetFont fnt_cut
Color 0,0,0
Text midx,midy,"Get Ready",1,1
Flip 
Delay 2000
End Function

Function clearup()

For abc.monst =Each monst
Delete abc
Next

For pb.bullet=Each bullet
Delete pb 
Next
pbcount=0

For dead.grave=Each grave
Delete dead
Next 

End Function


Function game_over() 
SetBuffer FrontBuffer()
PlaySound snd_dead
Delay 500
;Draws circular explosion thingy
For loop#=0 To 512 Step .5 
s=loop#/2
c=255-s
Color c,c,c
Oval player_x-s,player_y-s,loop#,loop#,0
Next

Delay 500
Color 0,0,0

For loop=0 To 512 Step .2  
s=loop/2
Oval player_x-s,player_y-s,loop,loop,1
Next


Delay 3000
levelend=True
update_highscores(score)
End Function

;Fills table with initial blank values
Function set_highscores(ns)
For loop=0 To ns
hst(loop)=0
hsnames$(loop,1)="Pongo"
hsnames$(loop,0)="Pongo"
Next
End Function

;Displays High Score Table
Function display_highscores(ns)
Color 255,255,255
Text 450,c+60,"High Scores",0,0
d=c+80
For loop=0 To ns
y=d+loop*10
Text 450,y,hst(loop),0,0
Text 500,y,hsnames$(loop,0),0,0
Next
End Function

;Places new score in High Score Table if value is high enough
Function update_highscores(score)

;Checks high score table to see if the new score should be included.
;If new score is to be included its position in table is stored in np.
;-1 value of np is used to signify that score should not be added because it is too low.

np=-1 
count=0
Repeat
If score>=hst(count) Then np=count
count=count+1 
Until np<>-1 Or count>ns


;If new score to be inclued, existing entries from bottom of table to np are moved down one place.
;Except when new score is to be added to the final position (not necessary in such a case).
If np<>-1 And  np<>ns Then 
For loop=ns To np+1 Step -1
hst(loop)=hst(loop-1)
hsnames$(loop,0)=hsnames$(loop-1,0)
Next
End If 

If np<>-1 Then 
;adds new score to high score table at position np. 
hst(np)=score
;requests player's name name and stores it in table.
hsnames$(np,0)=inputname$(np)

;Removes spaces from name and replaces them with underscores and stores new version in hsnames$(np,1).
hsnames$(np,1)=""
length=Len(hsnames$(np,0))
For loop=1 To length 
temp$=Mid$(hsnames$(np,0),loop,1)
If temp$=" " Then temp$="_"
hsnames$(np,1)=hsnames$(np,1)+temp$
Next

End If
End Function

;Input new name for highscore table. (np passed to this function only so it can be quoted in message)
Function inputname$(np)
ClsColor 0,0,200
Cls
PlaySound snd_clap
SetFont fnt_cut
Color 255,255,0
Text midx,H(1),"Congratulations !",1,1
Color 0,255,255
SetFont fnt_txt_big
Text midx,H(5),"Your score of "+score,1,1 

Text midx,H(6),"has placed you at position "+(np+1),1,1
Text midx,H(7),"in the High Score Table",1,1
Delay 500
Color 255,255,255
Text midx,H(10),"Please type your name",1,1
Repeat
Locate midx-100,H(11)
Color 0,0,200
Rect midx-100,H(11),600,20
Color 255,255,255
DrawImage eltypeimg,midx*.65,H(14) 
a$=Input("What is your name? ")
If Len (a$)>17  Then
Color 255,100,200
Text midx,H(13),"Sorry too long. Please type again.",1,1
Color 255,255,255
End If 
Until Len(a$)<16
If a$="" Then a$="anon"
Return a$
End Function 




 