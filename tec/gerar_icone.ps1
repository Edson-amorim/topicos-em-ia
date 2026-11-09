Add-Type -AssemblyName System.Drawing

function Gerar-IconeComandaFalada {
    param(
        [int]$Size,
        [string]$OutputPath
    )

    $dir = Split-Path -Parent $OutputPath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }

    $bmp = New-Object System.Drawing.Bitmap($Size, $Size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)

    $s = [float]$Size

    # 1. Fundo arredondado azul-marinho escuro com borda dourada de alto contraste
    $margin = $s * 0.03
    $rectSize = $s - (2 * $margin)
    $radius = $s * 0.22

    $pathBg = New-Object System.Drawing.Drawing2D.GraphicsPath
    $d = $radius * 2
    $pathBg.AddArc($margin, $margin, $d, $d, 180, 90)
    $pathBg.AddArc($margin + $rectSize - $d, $margin, $d, $d, 270, 90)
    $pathBg.AddArc($margin + $rectSize - $d, $margin + $rectSize - $d, $d, $d, 0, 90)
    $pathBg.AddArc($margin, $margin + $rectSize - $d, $d, $d, 90, 90)
    $pathBg.CloseFigure()

    $brushBg = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
        (New-Object System.Drawing.PointF(0, 0)),
        (New-Object System.Drawing.PointF($s, $s)),
        [System.Drawing.Color]::FromArgb(255, 15, 23, 42),
        [System.Drawing.Color]::FromArgb(255, 30, 58, 138)
    )
    $g.FillPath($brushBg, $pathBg)

    $penBorder = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 250, 204, 21), ($s * 0.032))
    $g.DrawPath($penBorder, $pathBg)

    # 2. Símbolo Universal da Acessibilidade no topo central (círculo azul-celeste + figura humana de braços abertos)
    $accCx = $s * 0.50
    $accCy = $s * 0.24
    $accR  = $s * 0.145

    $brushAccCircle = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 2, 132, 199))
    $g.FillEllipse($brushAccCircle, ($accCx - $accR), ($accCy - $accR), ($accR * 2), ($accR * 2))

    $penAccRing = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 250, 204, 21), ($s * 0.022))
    $g.DrawEllipse($penAccRing, ($accCx - $accR), ($accCy - $accR), ($accR * 2), ($accR * 2))

    # Figura do Símbolo da Acessibilidade (Vitruviano / ONU)
    $headR = $s * 0.026
    $brushWhite = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $g.FillEllipse($brushWhite, ($accCx - $headR), ($accCy - $accR * 0.68 - $headR), ($headR * 2), ($headR * 2))

    $penAccFig = New-Object System.Drawing.Pen([System.Drawing.Color]::White, ($s * 0.022))
    $penAccFig.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penAccFig.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    # Braços abertos
    $g.DrawLine($penAccFig, ($accCx - $accR * 0.62), ($accCy - $accR * 0.20), ($accCx + $accR * 0.62), ($accCy - $accR * 0.20))
    # Tronco
    $g.DrawLine($penAccFig, $accCx, ($accCy - $accR * 0.30), $accCx, ($accCy + $accR * 0.18))
    # Pernas abertas
    $g.DrawLine($penAccFig, $accCx, ($accCy + $accR * 0.18), ($accCx - $accR * 0.42), ($accCy + $accR * 0.68))
    $g.DrawLine($penAccFig, $accCx, ($accCy + $accR * 0.18), ($accCx + $accR * 0.42), ($accCy + $accR * 0.68))

    # 3. Pessoa com Deficiência à esquerda (cadeirante / pessoa com deficiência) e Pessoa sem Deficiência à direita
    $penPersonLeft = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 56, 189, 248), ($s * 0.036))
    $penPersonLeft.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penPersonLeft.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $brushPersonLeft = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 56, 189, 248))

    # Cabeça da pessoa à esquerda (Pessoa com Deficiência)
    $p1Hx = $s * 0.23
    $p1Hy = $s * 0.47
    $pHeadR = $s * 0.055
    $g.FillEllipse($brushPersonLeft, ($p1Hx - $pHeadR), ($p1Hy - $pHeadR), ($pHeadR * 2), ($pHeadR * 2))

    # Tronco e assento da cadeira de rodas / pessoa sentada à esquerda
    $g.DrawLine($penPersonLeft, $p1Hx, ($p1Hy + $pHeadR), $p1Hx, ($s * 0.69))
    $g.DrawLine($penPersonLeft, $p1Hx, ($s * 0.69), ($s * 0.31), ($s * 0.69))
    $g.DrawLine($penPersonLeft, ($s * 0.31), ($s * 0.69), ($s * 0.33), ($s * 0.82))

    # Roda da cadeira de rodas (símbolo claro de pessoa com deficiência)
    $wheelCx = $s * 0.21
    $wheelCy = $s * 0.76
    $wheelR  = $s * 0.085
    $penWheel = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 250, 204, 21), ($s * 0.028))
    $g.DrawEllipse($penWheel, ($wheelCx - $wheelR), ($wheelCy - $wheelR), ($wheelR * 2), ($wheelR * 2))

    # Braço da pessoa com deficiência segurando o lado esquerdo do cardápio
    $g.DrawLine($penPersonLeft, $p1Hx, ($s * 0.58), ($s * 0.37), ($s * 0.62))

    # Pessoa à direita (Pessoa sem deficiência em pé ao lado, em união/inclusão)
    $penPersonRight = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 74, 222, 128), ($s * 0.036))
    $penPersonRight.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penPersonRight.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $brushPersonRight = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 74, 222, 128))

    $p2Hx = $s * 0.77
    $p2Hy = $s * 0.45
    $g.FillEllipse($brushPersonRight, ($p2Hx - $pHeadR), ($p2Hy - $pHeadR), ($pHeadR * 2), ($pHeadR * 2))

    # Tronco e pernas da pessoa à direita
    $g.DrawLine($penPersonRight, $p2Hx, ($p2Hy + $pHeadR), $p2Hx, ($s * 0.71))
    $g.DrawLine($penPersonRight, $p2Hx, ($s * 0.71), ($s * 0.72), ($s * 0.85))
    $g.DrawLine($penPersonRight, $p2Hx, ($s * 0.71), ($s * 0.82), ($s * 0.85))

    # Braço da pessoa à direita segurando o lado direito do cardápio
    $g.DrawLine($penPersonRight, $p2Hx, ($s * 0.57), ($s * 0.63), ($s * 0.62))

    # 4. Cardápio Aberto no Centro segurado pelas duas pessoas (livro/cardápio dourado e branco com linhas e talheres)
    $menuLeft   = $s * 0.34
    $menuTop    = $s * 0.50
    $menuWidth  = $s * 0.32
    $menuHeight = $s * 0.32

    $brushMenuCover = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 250, 204, 21))
    $g.FillRectangle($brushMenuCover, ($menuLeft - $s * 0.015), ($menuTop - $s * 0.015), ($menuWidth + $s * 0.03), ($menuHeight + $s * 0.03))

    $brushMenuPage = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 255, 251, 235))
    $g.FillRectangle($brushMenuPage, $menuLeft, $menuTop, $menuWidth, $menuHeight)

    # Divisória central do cardápio aberto (duas páginas)
    $penMenuSpine = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 180, 83, 9), ($s * 0.016))
    $g.DrawLine($penMenuSpine, ($s * 0.50), $menuTop, ($s * 0.50), ($menuTop + $menuHeight))

    # Linhas dos itens/preços nas páginas do cardápio
    $penMenuText = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 30, 41, 59), ($s * 0.018))
    $penMenuText.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $penMenuText.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    # Página esquerda (prato/cloche desenhado + linhas)
    $brushPlate = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 217, 119, 6))
    $g.FillPie($brushPlate, ($s * 0.37), ($s * 0.54), ($s * 0.10), ($s * 0.10), 180, 180)
    $g.DrawLine($penMenuText, ($s * 0.365), ($s * 0.595), ($s * 0.475), ($s * 0.595))
    $g.DrawLine($penMenuText, ($s * 0.37), ($s * 0.66), ($s * 0.47), ($s * 0.66))
    $g.DrawLine($penMenuText, ($s * 0.37), ($s * 0.73), ($s * 0.47), ($s * 0.73))

    # Página direita (linhas de itens e preços do cardápio)
    $g.DrawLine($penMenuText, ($s * 0.53), ($s * 0.56), ($s * 0.63), ($s * 0.56))
    $g.DrawLine($penMenuText, ($s * 0.53), ($s * 0.62), ($s * 0.63), ($s * 0.62))
    $g.DrawLine($penMenuText, ($s * 0.53), ($s * 0.68), ($s * 0.63), ($s * 0.68))
    $g.DrawLine($penMenuText, ($s * 0.53), ($s * 0.74), ($s * 0.61), ($s * 0.74))

    # Mãos das duas pessoas segurando as bordas do cardápio
    $handR = $s * 0.024
    $g.FillEllipse($brushPersonLeft, ($menuLeft - $handR * 0.5), ($s * 0.62 - $handR), ($handR * 2), ($handR * 2))
    $g.FillEllipse($brushPersonRight, ($menuLeft + $menuWidth - $handR * 1.5), ($s * 0.62 - $handR), ($handR * 2), ($handR * 2))

    $bmp.Save($OutputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
}

$baseRes = "C:\topicos-em-ia\tec\android_app\res"
Gerar-IconeComandaFalada -Size 48  -OutputPath "$baseRes\mipmap-mdpi\ic_launcher.png"
Gerar-IconeComandaFalada -Size 72  -OutputPath "$baseRes\mipmap-hdpi\ic_launcher.png"
Gerar-IconeComandaFalada -Size 96  -OutputPath "$baseRes\mipmap-xhdpi\ic_launcher.png"
Gerar-IconeComandaFalada -Size 144 -OutputPath "$baseRes\mipmap-xxhdpi\ic_launcher.png"
Gerar-IconeComandaFalada -Size 192 -OutputPath "$baseRes\mipmap-xxxhdpi\ic_launcher.png"
Gerar-IconeComandaFalada -Size 512 -OutputPath "C:\topicos-em-ia\tec\icone_comanda_falada.png"

Write-Host ">>> Ícones gerados com sucesso!"
