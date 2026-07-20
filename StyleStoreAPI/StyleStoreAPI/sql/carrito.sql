-- ==========================================
-- MIGRACIÓN: Carrito de compras
-- Corre esto UNA VEZ en SSMS, conectado a StyleStoreDB.
-- No modifica ninguna tabla existente, solo agrega 2 tablas nuevas.
-- ==========================================
USE StyleStoreDB;
GO

CREATE TABLE Carrito(
    IdCarrito INT IDENTITY(1,1) PRIMARY KEY,
    IdUsuario INT NOT NULL UNIQUE,  -- un carrito activo por usuario
    FechaCreacion DATETIME DEFAULT GETDATE(),

    CONSTRAINT FK_CarritoUsuario
        FOREIGN KEY(IdUsuario)
        REFERENCES Usuarios(IdUsuario)
);
GO

CREATE TABLE DetalleCarrito(
    IdDetalleCarrito INT IDENTITY(1,1) PRIMARY KEY,
    IdCarrito INT NOT NULL,
    IdProducto INT NOT NULL,
    Cantidad INT NOT NULL CHECK (Cantidad > 0),

    CONSTRAINT FK_DetalleCarritoCarrito
        FOREIGN KEY(IdCarrito) REFERENCES Carrito(IdCarrito),

    CONSTRAINT FK_DetalleCarritoProducto
        FOREIGN KEY(IdProducto) REFERENCES Productos(IdProducto),

    -- Un mismo producto no puede estar 2 veces en el mismo carrito
    -- (si el usuario agrega el mismo producto otra vez, se suma la cantidad).
    CONSTRAINT UQ_CarritoProducto UNIQUE(IdCarrito, IdProducto)
);
GO

CREATE INDEX IX_DetalleCarrito_Carrito ON DetalleCarrito(IdCarrito);
GO
