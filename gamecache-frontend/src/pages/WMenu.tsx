import React from 'react';
import { AppBar, Button, MenuList, MenuListItem, Separator, TextInput, Toolbar } from 'react95';
import { useState } from 'react';
import w95 from '/w95.png';

const WMenu: React.FC = () => {
    const [open, setOpen] = useState(false);

    return (
        <AppBar>
            <Toolbar style={{ justifyContent: 'space-between' }}>
                <div style={{ position: 'relative', display: 'inline-block' }}>
                <Button
                    onClick={() => setOpen(!open)}
                    active={open}
                    style={{ fontWeight: 'bold' }}
                >
                    <img
                    src={w95}
                    alt='react95 logo'
                    style={{ height: '20px', marginRight: 4 }}
                    />
                    Start
                </Button>
                {open && (
                    <MenuList
                    style={{
                        position: 'absolute',
                        left: '0',
                        top: '100%'
                    }}
                    onClick={() => setOpen(false)}
                    >
                    <MenuListItem>
                        <span role='img' aria-label='👨‍💻'>
                        👨‍💻
                        </span>
                        Profile
                    </MenuListItem>
                    <MenuListItem>
                        <span role='img' aria-label='📁'>
                        📁
                        </span>
                        My account
                    </MenuListItem>
                    <Separator />
                    <MenuListItem disabled>
                        <span role='img' aria-label='🔙'>
                        🔙
                        </span>
                        Logout
                    </MenuListItem>
                    </MenuList>
                )}
                </div>

                <TextInput placeholder='Search...' width={150} />
            </Toolbar>
        </AppBar>
    );
};

export default WMenu;