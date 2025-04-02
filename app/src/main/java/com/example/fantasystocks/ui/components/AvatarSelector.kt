package com.example.fantasystocks.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fantasystocks.R

// List of available avatar resources
val avatarResources = listOf(
    R.drawable.avatar_1,
    R.drawable.avatar_2,
    R.drawable.avatar_3,
    R.drawable.avatar_4,
    R.drawable.avatar_5,
    R.drawable.avatar_6
)

@Composable
fun AvatarSelector(
    selectedAvatarId: Int = 0,
    onSelectAvatar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedAvatarId by remember { mutableIntStateOf(selectedAvatarId) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose an Avatar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    items(avatarResources.indices.toList()) { index ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (index == tempSelectedAvatarId) 3.dp else 1.dp,
                                    color = if (index == tempSelectedAvatarId) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { tempSelectedAvatarId = index }
                                .padding(if (index == tempSelectedAvatarId) 4.dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = avatarResources[index]),
                                contentDescription = "Avatar option ${index + 1}",
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSelectAvatar(tempSelectedAvatarId)
                    onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UserAvatar(
    avatarId: Int = 0,
    username: String = "",
    size: Int = 100,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .size(size.dp)
        .clip(CircleShape)
        .let { mod ->
            if (onClick != null) {
                mod.clickable(onClick = onClick)
            } else {
                mod
            }
        }
    
    if (avatarId in avatarResources.indices) {
        // Use selected avatar image
        Image(
            painter = painterResource(id = avatarResources[avatarId]),
            contentDescription = "User avatar",
            modifier = modifier
        )
    } else {
        // Fallback to text-based avatar
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (username.isNotEmpty()) username.take(1).uppercase() else "U",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}